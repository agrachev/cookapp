package ru.agrachev.cookapp.di

import ru.agrachev.cookapp.presentation.MainActivityViewModel
import ru.agrachev.parser.di.recipeParserModule
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.agrachev.cookapp.data.di.appRepositoryModule
import ru.agrachev.cookapp.data.di.packageContentRepositoryModule
import ru.agrachev.cookapp.domain.usecase.FetchRecipesUseCase

val appModule = module {
    includes(
        appRepositoryModule,
        recipeParserModule(packageContentRepositoryModule()),
    )
    singleOf(::FetchRecipesUseCase)
    viewModelOf(::MainActivityViewModel)
}
