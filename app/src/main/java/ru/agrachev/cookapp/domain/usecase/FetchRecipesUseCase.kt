package ru.agrachev.cookapp.domain.usecase

import ru.agrachev.parser.RecipeParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.transform
import ru.agrachev.cookapp.domain.repository.DocumentRepository

class FetchRecipesUseCase(
    private val documentRepository: DocumentRepository,
    private val recipeParser: RecipeParser
) {
    suspend operator fun invoke(
        url: String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) = flowOf(documentRepository.getDocument(url, dispatcher))
        .transform { document ->
            emit(recipeParser.parse(document))
        }
}
