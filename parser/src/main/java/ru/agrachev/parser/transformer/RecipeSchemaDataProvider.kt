package ru.agrachev.parser.transformer

import org.schema.model.CreativeWork
import org.schema.model.ItemList
import org.schema.model.Recipe
import org.schema.model.datatype.Text
import org.schema.model.impl.HowToStepImpl

class RecipeSchemaDataProvider(
    override val parent: Recipe,
) : CustomSchemaDataProvider<Recipe>, Recipe by parent {

    override fun <T : Any?> getRecipeInstructionsList(): List<T>? {
        val data = parent.getRecipeInstructionsList<T>()
        if (!data.isNullOrEmpty()) {
            val dataCopy = data.toList()
            data.clear()
            dataCopy.forEach {
                when (it) {
                    is ItemList -> parent.addRecipeInstructions(it)
                    is CreativeWork -> parent.addRecipeInstructions(it)
                    is Text -> HowToStepImpl().apply {
                        addName(it)
                        parent.addRecipeInstructions(this as ItemList)
                    }
                }
            }
        }
        return data
    }
}
