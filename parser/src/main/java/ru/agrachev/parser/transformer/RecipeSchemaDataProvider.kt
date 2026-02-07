package ru.agrachev.parser.transformer

import org.schema.model.Recipe
import org.schema.model.datatype.Text
import org.schema.model.impl.HowToStepImpl

class RecipeSchemaDataProvider(
    override val parent: Recipe,
) : CustomSchemaDataProvider<Recipe>, Recipe by parent {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> getRecipeInstructionsList(): List<T>? =
        parent.getRecipeInstructionsList<T>()
            ?.map {
                if (it is Text) HowToStepImpl().apply {
                    addName(it)
                } as T else it
            }
}
