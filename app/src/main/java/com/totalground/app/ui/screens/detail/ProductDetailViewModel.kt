package com.totalground.app.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.totalground.app.data.model.ApiProductDetail
import com.totalground.app.data.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProductDetailUiState {
    object Loading : ProductDetailUiState()
    data class Success(val product: ApiProductDetail) : ProductDetailUiState()
    data class Error(val message: String) : ProductDetailUiState()
}

class ProductDetailViewModel(
    private val repository: CatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    fun loadProductDetail(slug: String, language: String = "es") {
        viewModelScope.launch {
            _uiState.value = ProductDetailUiState.Loading
            repository.getProductDetail(slug, language)
                .onSuccess { detail ->
                    _uiState.value = ProductDetailUiState.Success(detail)
                }
                .onFailure { exception ->
                    _uiState.value = ProductDetailUiState.Error(
                        exception.localizedMessage ?: "No se pudo obtener la información del producto."
                    )
                }
        }
    }
}
