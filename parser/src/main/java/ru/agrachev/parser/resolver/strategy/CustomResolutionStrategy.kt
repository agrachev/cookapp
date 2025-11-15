package ru.agrachev.parser.resolver.strategy

import ru.agrachev.parser.builder.JsonLdBuilderProvider
import ru.agrachev.parser.builder.JsonLdValue
import ru.agrachev.parser.di.ListOfResolverCreators
import com.weedow.schemaorg.commons.model.JsonLdNode
import org.jsoup.nodes.Element

internal class CustomResolutionStrategy<T : JsonLdNode>(
    override val builderProvider: JsonLdBuilderProvider,
    resolverInitializer: ListOfResolverCreators<T>.() -> Unit,
) : ResolutionStrategy {

    val list = buildList {
        resolverInitializer()
    }

    override fun resolve(
        entity: JsonLdValue?,
        entityClass: Class<out JsonLdValue>,
        element: Element?,
        rootElement: Element
    ): JsonLdValue? {
        //val n = list[0]().call(rootEntity)
        TODO("Not yet implemented")
    }

    override fun resolve(
        entity: JsonLdNode?,
        entityClass: Class<out JsonLdNode>,
        element: Element?,
        rootElement: Element
    ): JsonLdNode? {
        TODO("Not yet implemented")
    }
}
