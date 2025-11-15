package ru.agrachev.parser

import com.weedow.schemaorg.commons.model.JsonLdNode
import com.weedow.schemaorg.serializer.JsonLdException
import com.weedow.schemaorg.serializer.deserialization.JsonLdDeserializer
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.schema.model.Recipe
import ru.agrachev.parser.builder.JsonLdBuilderProvider
import ru.agrachev.parser.validator.JsonLdValidator

class RecipeParser internal constructor(
    private val validator: JsonLdValidator,
    private val deserializer: JsonLdDeserializer,
    private val builders: JsonLdBuilderProvider,
) {
    fun parse(document: Document): List<Recipe> {
        val recipeElements = document.select(GET_RECIPE_ELEMENTS)
        var elementIndex = 0
        return document.select(GET_JSON_LD_SECTIONS)
            .mapNotNull { element ->
                try {
                    //throw JsonLdException("Hello", Exception())
                    val json = validator.validate(element.html())
                    deserializer.deserialize(json) as JsonLdNode?
                } catch (_: JsonLdException) {
                    null
                }
            }
            .filterIsInstance<Recipe>()
            .onEachIndexed { index, recipe ->
                when {
                    recipeElements.isEmpty() -> recipe.decorate(document)
                    index < recipeElements.size -> recipe.decorate(
                        recipeElements[elementIndex++]
                    )
                }
            }.plus(
                List(
                    recipeElements.subList(
                        elementIndex,
                        recipeElements.size,
                    ).size
                ) { index ->
                    builders.getImplementation<Recipe>().apply {
                        decorate(recipeElements[index])
                    }
                }
            ).apply {
                println(this)
            }
    }

    internal inline fun <reified T : JsonLdNode> JsonLdBuilderProvider.getImplementation() =
        this.provideImplementation(T::class.java)

    internal inline fun <reified T : JsonLdNode> T.decorate(element: Element) =
        T::class.java.let { clazz ->
            builders
                .provideNodeBuilder(clazz)
                .build(element, this)
        }

    companion object {
        private const val GET_JSON_LD_SECTIONS = "script[type=application/ld+json]"
        private const val GET_RECIPE_ELEMENTS = "[itemtype~=https?://schema.org/Recipe]"
    }
}
