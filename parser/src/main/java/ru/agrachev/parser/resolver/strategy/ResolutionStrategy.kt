package ru.agrachev.parser.resolver.strategy

import ru.agrachev.parser.builder.JsonLdBuilderProvider
import ru.agrachev.parser.builder.JsonLdValue
import com.weedow.schemaorg.commons.model.JsonLdNode
import org.jsoup.nodes.Element

internal interface ResolutionStrategy {

    val builderProvider: JsonLdBuilderProvider

    fun resolve(
        entity: JsonLdValue?,
        entityClass: Class<out JsonLdValue>,
        element: Element?,
        rootElement: Element,
    ): JsonLdValue?

    fun resolve(
        entity: JsonLdNode?,
        entityClass: Class<out JsonLdNode>,
        element: Element?,
        rootElement: Element,
    ): JsonLdNode?
}
