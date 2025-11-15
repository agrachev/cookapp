package ru.agrachev.cookapp.domain.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.jsoup.nodes.Document

interface DocumentRepository {
    suspend fun getDocument(url: String, dispatcher: CoroutineDispatcher = Dispatchers.IO): Document
}
