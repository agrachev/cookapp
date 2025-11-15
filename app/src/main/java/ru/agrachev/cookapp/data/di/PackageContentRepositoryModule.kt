package ru.agrachev.cookapp.data.di

import ru.agrachev.parser.data.PackageContentRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.agrachev.cookapp.data.AndroidPackageContentRepository

fun packageContentRepositoryModule() = module {
    singleOf(::AndroidPackageContentRepository) {
        bind<PackageContentRepository>()
    }
}
