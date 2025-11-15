package ru.agrachev.parser.resolver.strategy

import ru.agrachev.parser.builder.JsonLdBuilderProvider
import ru.agrachev.parser.builder.JsonLdValue
import com.weedow.schemaorg.commons.model.JsonLdNode
import org.jsoup.nodes.Element

internal class ParsingResolutionStrategy(
    override val builderProvider: JsonLdBuilderProvider,
) : ResolutionStrategy {

    override fun resolve(
        entity: JsonLdValue?,
        entityClass: Class<out JsonLdValue>,
        element: Element?,
        rootElement: Element,
    ): JsonLdValue? =
        entity ?: (element?.let {
            builderProvider
                .provideValueBuilder()
                .build(it, entityClass)
        })

    override fun resolve(
        entity: JsonLdNode?,
        entityClass: Class<out JsonLdNode>,
        element: Element?,
        rootElement: Element,
    ): JsonLdNode? =
        element?.let {
            (entity ?: builderProvider.provideImplementation(
                getImplementationClass(
                    element, entityClass
                )
            )).apply {
                builderProvider
                    .provideNodeBuilder(entityClass)
                    .build(element, this)
            }
        } ?: entity

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> getImplementationClass(
        element: Element,
        entityClass: Class<out T>
    ): Class<out T> = element.attribute(ITEM_TYPE_KEY)?.let {
        Class.forName("${entityClass.`package`.name}.${it.value.substringAfterLast('/')}") as Class<out T>
    } ?: entityClass
}

private const val ITEM_TYPE_KEY = "itemtype"
