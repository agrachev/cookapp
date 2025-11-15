package ru.agrachev.parser.resolver.strategy

import ru.agrachev.parser.builder.JsonLdValue
import com.weedow.schemaorg.commons.model.JsonLdNode
import org.jsoup.nodes.Element
import ru.agrachev.parser.isCompleted

internal class ResolutionContainer(
    val resolutionStrategies: Collection<ResolutionStrategy>,
) {
    fun resolveSchemaValue(
        name: String,
        entities: List<JsonLdValue>,
        rootElement: Element,
        entityClass: Class<out JsonLdValue>,
    ): List<JsonLdValue> =
        resolveStrategies(name, entities, rootElement) { entity, element, rootElement ->
            resolve(entity, entityClass, element, rootElement)
        }

    fun resolveSchemaNode(
        name: String,
        entities: List<JsonLdNode>,
        rootElement: Element,
        entityClass: Class<out JsonLdNode>,
    ): List<JsonLdNode> =
        resolveStrategies(name, entities, rootElement) { entity, element, rootElement ->
            resolve(entity, entityClass, element, rootElement)
        }

    private inline fun <reified C> resolveStrategies(
        name: String,
        entities: List<C & Any>,
        rootElement: Element,
        entityResolver: ResolutionStrategy.(C?, Element?, Element) -> C?
    ): List<C & Any> {
        val elements by lazy(mode = LazyThreadSafetyMode.NONE) {
            rootElement.select("[itemprop=$name]")
        }
        val resolutionStrategyIterator by lazy(mode = LazyThreadSafetyMode.NONE) {
            resolutionStrategies.iterator()
        }
        var result = entities
        while (!result.isCompleted() && resolutionStrategyIterator.hasNext()) {
            result = resolutionStrategyIterator.next().run {
                (0..<maxOf(result.size, elements.size.coerceAtLeast(1)))
                    .mapNotNull { index ->
                        val item = result.getOrNull(index)
                        if (!item.isCompleted()) {
                            entityResolver(item, elements.getOrNull(index).let {
                                if (it != rootElement) it else null
                            }, rootElement)
                        } else {
                            item
                        }
                    }
            }
        }
        return result
    }
}
