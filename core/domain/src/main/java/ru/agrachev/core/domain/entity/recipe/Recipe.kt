package ru.agrachev.core.domain.entity.recipe

import java.net.URI
import java.net.URL

data class Recipe(
    val title: String,
    val resultPhotos: List<URI>,
    val sourceUrl: URL? = null,
    val description: String? = null,
    val ingredients: List<RecipeIngredient>,
    val instructions: List<RecipeInstruction>,
    val numberOfServings: Int = 1,
)
