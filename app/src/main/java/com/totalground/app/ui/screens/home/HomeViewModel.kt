package com.totalground.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.totalground.app.data.model.ApiBanner
import com.totalground.app.data.model.ApiCategory
import com.totalground.app.data.model.ApiProduct
import com.totalground.app.data.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val banners: List<ApiBanner>,
        val categories: List<ApiCategory>,
        val featuredProducts: List<ApiProduct>
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(
    private val repository: CatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData(language: String = "es") {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            repository.getHome(language)
                .onSuccess { response ->
                    _uiState.value = HomeUiState.Success(
                        banners = response.banners,
                        categories = response.categories,
                        featuredProducts = response.featuredProducts
                    )
                }
                .onFailure { exception ->
                    _uiState.value = HomeUiState.Error(
                        exception.localizedMessage ?: "No se pudo cargar el contenido de inicio."
                    )
                }
        }
    }
}
