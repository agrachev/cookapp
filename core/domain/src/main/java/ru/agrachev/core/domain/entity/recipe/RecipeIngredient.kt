package ru.agrachev.core.domain.entity.recipe

data class RecipeIngredient(
    val description: String,
    val quantity: Float,
    val measurementType: MeasurementType,
)
