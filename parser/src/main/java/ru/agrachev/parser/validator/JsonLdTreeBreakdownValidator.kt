package ru.agrachev.parser.validator

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.databind.node.ObjectNode
import com.weedow.schemaorg.commons.model.JsonLdNode
import ru.agrachev.parser.JsonLdTypeMapper
import ru.agrachev.parser.isSchemaNode
import ru.agrachev.parser.typeClass

internal class JsonLdTreeBreakdownValidator(
    private val typeMapper: JsonLdTypeMapper,
) : JsonLdValidator {

    private val objectMapper by lazy {
        ObjectMapper()
    }

    override fun validate(json: String) =
        objectMapper.readTree(json).let {
            validateObject(it)
            it.toString()
        }

    private fun validateObject(objectNode: JsonNode) {
        objectNode.get(TYPE_PROPERTY)?.let { classTypeNode ->
            typeMapper[classTypeNode.asText()]?.let { schemaClass ->
                objectNode.properties()
                    .forEach { (property, node) ->
                        when {
                            node.isValueNode -> {
                                validateValue(
                                    property, node, schemaClass,
                                )?.let { replaceWith ->
                                    with(objectNode as ObjectNode) {
                                        set<JsonNode>(property, replaceWith)
                                    }
                                }
                            }

                            node.isArray -> validateArray(property, node, schemaClass)
                            node.isObject -> validateObject(node)
                        }
                    }
            }
        }
    }

    private fun validateArray(
        property: String, arrayNode: JsonNode, schemaClass: Class<out JsonLdNode>,
    ) {
        arrayNode.elements().asSequence()
            .filter { it.isValueNode }
            .forEachIndexed { index, node ->
                validateValue(property, node, schemaClass)?.let { replaceWith ->
                    with(arrayNode as ArrayNode) {
                        set(index, replaceWith)
                    }
                }
            }
    }

    private fun validateValue(
        property: String, node: JsonNode, schemaClass: Class<out JsonLdNode>
    ): JsonNode? = schemaClass
        .getFieldOrNull(property)?.typeClass?.let { typeClass ->
            when {
                typeClass.isSchemaNode ->
                    objectMapper.valueToTree<JsonNode>(
                        NamedObject(
                            type = typeClass.simpleName,
                            identifier = node.asText(),
                        )
                    )

                // TODO move into separate property adjustor
                property == "commentCount" && node.nodeType == JsonNodeType.STRING ->
                    objectMapper.valueToTree<JsonNode>(node.asText().toInt())

                else -> null
            }
        }

    private fun Class<*>.getFieldOrNull(fieldName: String) = try {
        getDeclaredField(fieldName)
    } catch (_: Exception) {
        null
    }

    private data class NamedObject(
        @get:JsonProperty(TYPE_PROPERTY) val type: String,
        val identifier: String?,
    )
}

private const val TYPE_PROPERTY = "@type"
