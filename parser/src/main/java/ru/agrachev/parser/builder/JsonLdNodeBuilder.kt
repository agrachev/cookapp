package ru.agrachev.parser.builder

import com.weedow.schemaorg.commons.model.JsonLdDataType
import com.weedow.schemaorg.commons.model.JsonLdFieldTypes
import com.weedow.schemaorg.commons.model.JsonLdNode
import org.jsoup.nodes.Element
import ru.agrachev.parser.SchemaDataProvider
import ru.agrachev.parser.getOrDefault
import ru.agrachev.parser.isCompleted
import ru.agrachev.parser.isSchemaData
import ru.agrachev.parser.isSchemaNode
import ru.agrachev.parser.resolver.strategy.ResolutionContainer
import ru.agrachev.parser.toSchemaData
import ru.agrachev.parser.toSchemaNode
import ru.agrachev.parser.typeClass
import java.lang.reflect.Field

internal class JsonLdNodeBuilder<T : JsonLdNode> private constructor(
    private val resolutionContainer: ResolutionContainer,
    private val schemaDataProvider: SchemaDataProvider<T>? = null,
) : JsonLdTypeBuilder<T, T> {

    override fun build(element: Element, entity: T): T {
        val schemaDataProvider = schemaDataProvider.getOrDefault(entity)
        entity.javaClass.declaredFields.forEach { field ->
            val getter =
                schemaDataProvider.javaClass.declaredMethods.find { method -> method.name like "get${field.name}list" }
            val schemaData = getter?.invoke(schemaDataProvider)
            val result = field.typeClass
                .resolveSchemaEntities(schemaData, element, field.name) {
                    // Object type: multiple variants
                    field.findAnnotation<JsonLdFieldTypes>()?.let { annotation ->
                        annotation.value
                            .asSequence()
                            .mapNotNull {
                                it.javaObjectType
                                    .resolveSchemaEntities(
                                        schemaData,
                                        element,
                                        field.name,
                                    ) {
                                        null
                                    }
                            }
                            .firstOrNull { it.isCompleted() }
                    }
                }
            if (!result.isNullOrEmpty()) {
                field.isAccessible = true
                field.set(entity, result)
            }
        }
        return entity
    }

    private inline fun <T> Class<T>.resolveSchemaEntities(
        entities: Any?,
        element: Element,
        name: String,
        elseBranch: () -> List<Any>?,
    ) = when {
        isSchemaData -> resolutionContainer.resolveSchemaValue(
            name,
            entities.toTypedList<JsonLdDataType<*>>(),
            element,
            toSchemaData(),
        )

        isSchemaNode -> resolutionContainer.resolveSchemaNode(
            name,
            entities.toTypedList<JsonLdNode>(),
            element,
            toSchemaNode(),
        )

        else -> elseBranch()
    }

    internal class Builder<T : JsonLdNode>(
        private val resolutionContainer: ResolutionContainer,
    ) {

        private var schemaDataProvider: SchemaDataProvider<T>? = null

        inline fun transformer(noinline schemaDataProvider: SchemaDataProvider<T>) {
            this.schemaDataProvider = schemaDataProvider
        }
        //private val delegateCreators = mutableListOf<ResolverCreator<T>>()

        //fun resolver(creator: ResolverCreator<T>) = delegateCreators.add(creator)

        fun build() = JsonLdNodeBuilder(resolutionContainer, schemaDataProvider)
    }
}

inline fun <reified T : Annotation> Field.findAnnotation() =
    this.annotations.firstOrNull { it is T } as? T

inline infix fun String.like(other: String) = this.contentEquals(other, true)

inline fun <reified T> Any?.toTypedList() = (this as? List<*>)?.let { list ->
    list.mapNotNull { it as? T }
} ?: emptyList()
