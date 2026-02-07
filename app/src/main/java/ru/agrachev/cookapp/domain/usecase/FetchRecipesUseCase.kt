package ru.agrachev.cookapp.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.transform
import org.schema.model.Recipe
import ru.agrachev.cookapp.domain.repository.DocumentRepository
import ru.agrachev.parser.SchemaParser

class FetchRecipesUseCase(
    private val documentRepository: DocumentRepository,
    private val schemaParser: SchemaParser
) {
    suspend operator fun invoke(
        url: String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) = flowOf(documentRepository.getDocument(url, dispatcher))
        .transform { document ->
            emit(schemaParser.parse(document, Recipe::class.java).apply {
                //println(this)
            })
        }
}
