package ru.agrachev.cookapp.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ru.agrachev.cookapp.domain.repository.DocumentRepository

class JSoupDocumentRepository : DocumentRepository {
    override suspend fun getDocument(url: String, dispatcher: CoroutineDispatcher): Document =
        withContext(dispatcher) {
            //Jsoup.parse(url)
            Jsoup.connect(url).get()
        }
}
