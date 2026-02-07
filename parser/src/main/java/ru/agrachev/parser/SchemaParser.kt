package ru.agrachev.parser

import com.weedow.schemaorg.commons.model.JsonLdNode
import com.weedow.schemaorg.serializer.deserialization.JsonLdDeserializer
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import ru.agrachev.parser.builder.JsonLdBuilderProvider
import ru.agrachev.parser.validator.JsonLdValidator

class SchemaParser internal constructor(
    private val validator: JsonLdValidator,
    private val deserializer: JsonLdDeserializer,
    private val builders: JsonLdBuilderProvider,
) {

    fun <T : JsonLdNode> parse(document: Document, schemaClass: Class<T>): List<T> {
        val schemaElements = document.select(
            GET_SCHEMA_ELEMENTS.format(schemaClass.simpleName),
        )
        var elementIndex = 0
        return document.select(GET_JSON_LD_SECTIONS)
            .mapNotNull { element ->
                try {
                    //throw JsonLdException("Hello", Exception())
                    val json = validator.validate(element.html())
                    deserializer.deserialize(json) as JsonLdNode?
                } catch (_: Exception) {
                    null
                }
            }
            .filterIsInstance(schemaClass)
            .onEachIndexed { index, schemaNode ->
                when {
                    schemaElements.isEmpty() -> schemaNode.decorate(document, schemaClass)
                    index < schemaElements.size -> schemaNode.decorate(
                        schemaElements[elementIndex++], schemaClass
                    )
                }
            } + List(
            schemaElements.subList(
                elementIndex,
                schemaElements.size,
            ).size
        ) { index ->
            builders.provideImplementation(schemaClass).apply {
                decorate(schemaElements[index], schemaClass)
            }
        }
    }

    private fun <T : JsonLdNode> T.decorate(element: Element, schemaClass: Class<T>) =
        builders
            .provideNodeBuilder(schemaClass)
            .build(element, this)
}

private const val GET_JSON_LD_SECTIONS = "script[type=application/ld+json]"
private const val GET_SCHEMA_ELEMENTS = "[itemtype~=https?://schema.org/%s]"
