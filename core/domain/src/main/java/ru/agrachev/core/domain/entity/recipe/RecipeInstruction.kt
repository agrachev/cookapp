package ru.agrachev.core.domain.entity.recipe

import java.net.URI

data class RecipeInstruction(
    val description: String,
    val imageAttachments: List<URI> = emptyList(),
)
