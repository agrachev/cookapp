package ru.agrachev.parser.resolver.strategy

import com.weedow.schemaorg.commons.model.JsonLdNode
import org.jsoup.nodes.Element
import ru.agrachev.parser.builder.JsonLdBuilderProvider
import ru.agrachev.parser.builder.JsonLdValue
import ru.agrachev.parser.di.ListOfResolverCreators
import ru.agrachev.parser.isCompleted

@Suppress("UNCHECKED_CAST")
internal class CustomResolutionStrategy<T : JsonLdNode>(
    override val builderProvider: JsonLdBuilderProvider,
    private val resolverInitializers: ListOfResolverCreators<T>,
) : ResolutionStrategy {

    override fun resolve(
        entity: JsonLdValue?,
        entityClass: Class<out JsonLdValue>,
        element: Element?,
        rootElement: Element,
    ) = resolveEntity(entity, element, rootElement)

    override fun resolve(
        entity: JsonLdNode?,
        entityClass: Class<out JsonLdNode>,
        element: Element?,
        rootElement: Element,
    ) = resolveEntity(entity, element, rootElement)

    private inline fun <reified C> resolveEntity(
        entity: C?,
        element: Element?,
        rootElement: Element,
    ) = entity?.also {
        runCatching {
            (element ?: rootElement).let { anchorElement ->
                var resultEntity = it as T
                val resolverInitializersIterator by lazy(mode = LazyThreadSafetyMode.NONE) {
                    resolverInitializers.iterator()
                }
                while (!resultEntity.isCompleted() && resolverInitializersIterator.hasNext()) {
                    resultEntity = resolverInitializersIterator.next().run {
                        this.call().invoke(resultEntity, anchorElement)
                    }
                }
                resultEntity as? C
            }
        }.getOrDefault(it)
    }
}
