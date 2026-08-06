package com.totalground.app.ui.screens.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.totalground.app.data.model.ApiDocument
import com.totalground.app.data.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DocumentUiState {
    object Loading : DocumentUiState()
    data class Success(val documents: List<ApiDocument>) : DocumentUiState()
    object Empty : DocumentUiState()
    data class Error(val message: String) : DocumentUiState()
}

class DocumentViewModel(
    private val repository: CatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DocumentUiState>(DocumentUiState.Loading)
    val uiState: StateFlow<DocumentUiState> = _uiState.asStateFlow()

    init {
        loadDocuments()
    }

    fun loadDocuments(categoryKey: String? = null, query: String? = null, language: String = "es") {
        viewModelScope.launch {
            _uiState.value = DocumentUiState.Loading
            repository.getDocuments(categoryKey, query, 1, language)
                .onSuccess { response ->
                    val docs = response.data ?: emptyList()
                    if (docs.isEmpty()) {
                        _uiState.value = DocumentUiState.Empty
                    } else {
                        _uiState.value = DocumentUiState.Success(docs)
                    }
                }
                .onFailure { exception ->
                    _uiState.value = DocumentUiState.Error(
                        exception.localizedMessage ?: "No se pudieron obtener las fichas técnicas."
                    )
                }
        }
    }
}
