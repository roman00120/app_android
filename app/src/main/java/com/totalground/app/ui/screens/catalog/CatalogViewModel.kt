package com.totalground.app.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.totalground.app.data.model.ApiCategory
import com.totalground.app.data.model.ApiProduct
import com.totalground.app.data.repository.CatalogRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CatalogUiState {
    object Loading : CatalogUiState()
    data class Success(
        val products: List<ApiProduct>,
        val categories: List<ApiCategory>,
        val selectedCategoryKey: String? = null,
        val searchQuery: String = "",
        val sortBy: String = "sort_order",
        val currentPage: Int = 1,
        val totalPages: Int = 1,
        val totalProducts: Int = 0
    ) : CatalogUiState()
    object Empty : CatalogUiState()
    data class Error(val message: String) : CatalogUiState()
}

class CatalogViewModel(
    private val repository: CatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private var currentCategoryKey: String? = null
    private var currentQuery: String = ""
    private var currentSortBy: String = "sort_order"
    private var currentPage: Int = 1

    private var searchJob: Job? = null

    init {
        loadCatalog()
    }

    fun setCategory(categoryKey: String?) {
        currentCategoryKey = if (categoryKey == "all" || categoryKey == "todos" || categoryKey.isNullOrBlank()) null else categoryKey.trim()
        currentPage = 1
        loadCatalog()
    }

    fun onSearchQueryChanged(newQuery: String) {
        currentQuery = newQuery
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Debounce search
            currentPage = 1
            loadCatalog()
        }
    }

    fun setSortBy(sortBy: String) {
        currentSortBy = sortBy
        currentPage = 1
        loadCatalog()
    }

    fun loadCatalog(isNextPage: Boolean = false, language: String = "es") {
        viewModelScope.launch {
            if (!isNextPage) {
                _uiState.value = CatalogUiState.Loading
            }

            val categoriesResult = repository.getCategories(language)
            val categories = categoriesResult.getOrDefault(emptyList())

            repository.getProducts(
                categoryKey = currentCategoryKey,
                query = currentQuery.trim().ifBlank { null },
                sortBy = currentSortBy,
                page = currentPage,
                language = language
            ).onSuccess { response ->
                val newProducts = response.data ?: emptyList()

                if (newProducts.isEmpty()) {
                    _uiState.value = CatalogUiState.Empty
                } else {
                    _uiState.value = CatalogUiState.Success(
                        products = newProducts,
                        categories = categories,
                        selectedCategoryKey = currentCategoryKey,
                        searchQuery = currentQuery,
                        sortBy = currentSortBy,
                        currentPage = 1,
                        totalPages = 1,
                        totalProducts = newProducts.size
                    )
                }
            }.onFailure { exception ->
                _uiState.value = CatalogUiState.Error(
                    exception.localizedMessage ?: "Error al cargar los productos."
                )
            }
        }
    }
}
