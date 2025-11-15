package ru.agrachev.cookapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.agrachev.cookapp.domain.usecase.FetchRecipesUseCase

class MainActivityViewModel(
    private val fetchRecipes: FetchRecipesUseCase
) : ViewModel() {

    fun parseUrl(url: String) {
        viewModelScope.launch {
            fetchRecipes(url)
                .collect {
                    //println(it)
                }
        }
    }
}
