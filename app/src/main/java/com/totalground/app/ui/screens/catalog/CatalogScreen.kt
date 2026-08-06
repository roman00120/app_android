package com.totalground.app.ui.screens.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.totalground.app.ui.components.ErrorState
import com.totalground.app.ui.components.LoadingState
import com.totalground.app.ui.components.ProductCard
import com.totalground.app.ui.theme.TotalGroundNavy
import com.totalground.app.ui.theme.TotalGroundRed

@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel,
    initialCategoryKey: String? = null,
    initialQuery: String? = null,
    onProductClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchInput by remember { mutableStateOf(initialQuery ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Search & Filter Header Bar
        CatalogHeader(
            searchQuery = searchInput,
            onQueryChange = {
                searchInput = it
                viewModel.onSearchQueryChanged(it)
            },
            onClearSearch = {
                searchInput = ""
                viewModel.onSearchQueryChanged("")
            }
        )

        when (val state = uiState) {
            is CatalogUiState.Loading -> LoadingState(message = "Cargando catálogo...")
            is CatalogUiState.Error -> ErrorState(message = state.message, onRetry = { viewModel.loadCatalog() })
            is CatalogUiState.Empty -> EmptyCatalogState(onReset = {
                searchInput = ""
                viewModel.onSearchQueryChanged("")
                viewModel.setCategory(null)
            })
            is CatalogUiState.Success -> CatalogContent(
                state = state,
                onCategorySelect = { viewModel.setCategory(it) },
                onSortSelect = { viewModel.setSortBy(it) },
                onProductClick = onProductClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogHeader(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Buscar producto, modelo o código...",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = TotalGroundNavy
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Borrar búsqueda",
                                tint = TotalGroundRed
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F5F9),
                    unfocusedContainerColor = Color(0xFFF1F5F9),
                    focusedBorderColor = TotalGroundRed,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TotalGroundNavy,
                    unfocusedTextColor = TotalGroundNavy
                ),
                singleLine = true
            )
        }
    }
}

@Composable
private fun CatalogContent(
    state: CatalogUiState.Success,
    onCategorySelect: (String?) -> Unit,
    onSortSelect: (String) -> Unit,
    onProductClick: (String) -> Unit
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Category Scrollable Chips Row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                CategoryPill(
                    title = "Todos",
                    isSelected = state.selectedCategoryKey == null,
                    onClick = { onCategorySelect(null) }
                )
            }
            items(state.categories) { category ->
                CategoryPill(
                    title = category.name,
                    isSelected = state.selectedCategoryKey == category.key,
                    onClick = { onCategorySelect(category.key) }
                )
            }
        }

        // Search Stats & Sort Dropdown
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${state.totalProducts} Productos reales",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TotalGroundNavy.copy(alpha = 0.8f)
            )

            Box {
                Row(
                    modifier = Modifier.clickable { sortMenuExpanded = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Ordenar",
                        tint = TotalGroundRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (state.sortBy) {
                            "name" -> "Nombre"
                            "price" -> "Precio"
                            else -> "Relevancia"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TotalGroundRed
                    )
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Relevancia") },
                        onClick = {
                            onSortSelect("sort_order")
                            sortMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Nombre A-Z") },
                        onClick = {
                            onSortSelect("name")
                            sortMenuExpanded = false
                        }
                    )
                }
            }
        }

        // Product Grid (2 Columns)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.products) { product ->
                ProductCard(
                    product = product,
                    onClick = { onProductClick(product.slug) }
                )
            }
        }
    }
}

@Composable
private fun CategoryPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) TotalGroundRed else Color.White
    val textColor = if (isSelected) Color.White else TotalGroundNavy

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun EmptyCatalogState(onReset: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = "Sin resultados",
                modifier = Modifier.size(64.dp),
                tint = TotalGroundRed
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No se encontraron productos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TotalGroundNavy
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Intenta con otro término o selecciona la categoría 'Todos'.",
                fontSize = 14.sp,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(containerColor = TotalGroundRed)
            ) {
                Text("Limpiar Búsqueda", color = Color.White)
            }
        }
    }
}
