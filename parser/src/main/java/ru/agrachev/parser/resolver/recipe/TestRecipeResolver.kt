package ru.agrachev.parser.resolver.recipe

import org.jsoup.nodes.Element
import org.schema.model.ItemList
import org.schema.model.Recipe
import org.schema.model.datatype.Text
import org.schema.model.impl.HowToStepImpl
import ru.agrachev.parser.resolver.CustomResolver

internal class TestRecipeResolver(
    override val parent: Recipe,
    override val element: Element,
    override val entity: Any,
) : CustomResolver<Recipe>, Recipe by parent {

    override fun <T : Any?> getRecipeInstructionsList(): List<T>? =
        parent.getRecipeInstructionsList<T>().apply {
            val mappedList = this
                .asSequence()
                .filterIsInstance<Text>()
                .map {
                    HowToStepImpl().apply {
                        addText(it)
                    } as ItemList
                }
            if (mappedList.any()) {
                clear()
                mappedList.forEach {
                    parent.addRecipeInstructions(it)
                }
            }
        }
}
