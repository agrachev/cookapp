package ru.agrachev.cookapp.data.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.agrachev.cookapp.data.JSoupDocumentRepository
import ru.agrachev.cookapp.domain.repository.DocumentRepository

val appRepositoryModule = module {
    singleOf(::JSoupDocumentRepository) {
        bind<DocumentRepository>()
    }
}
