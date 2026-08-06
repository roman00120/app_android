package com.totalground.app.ui.screens.home

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.totalground.app.data.model.ApiCategory
import com.totalground.app.data.model.ApiProduct
import com.totalground.app.ui.components.BannerSlider
import com.totalground.app.ui.components.ErrorState
import com.totalground.app.ui.components.LoadingState
import com.totalground.app.ui.components.ProductCard

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCategoryClick: (String) -> Unit,
    onProductClick: (String) -> Unit,
    onNavigateCatalog: () -> Unit,
    onNavigateDocuments: () -> Unit,
    onNavigateContact: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is HomeUiState.Loading -> LoadingState(message = "Cargando catálogo...")
        is HomeUiState.Error -> ErrorState(message = state.message, onRetry = { viewModel.loadHomeData() })
        is HomeUiState.Success -> HomeContent(
            state = state,
            onCategoryClick = onCategoryClick,
            onProductClick = onProductClick,
            onNavigateCatalog = onNavigateCatalog,
            onNavigateDocuments = onNavigateDocuments,
            onNavigateContact = onNavigateContact
        )
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState.Success,
    onCategoryClick: (String) -> Unit,
    onProductClick: (String) -> Unit,
    onNavigateCatalog: () -> Unit,
    onNavigateDocuments: () -> Unit,
    onNavigateContact: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Banners
        if (state.banners.isNotEmpty()) {
            BannerSlider(
                banners = state.banners,
                onBannerClick = { onNavigateCatalog() }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Quick Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionChip(
                title = "Catálogo",
                icon = Icons.Default.ElectricBolt,
                modifier = Modifier.weight(1f),
                onClick = onNavigateCatalog
            )
            QuickActionChip(
                title = "Fichas",
                icon = Icons.Default.Description,
                modifier = Modifier.weight(1f),
                onClick = onNavigateDocuments
            )
            QuickActionChip(
                title = "Cotizar",
                icon = Icons.Default.Mail,
                modifier = Modifier.weight(1f),
                onClick = onNavigateContact
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Categories Section
        SectionHeader(
            title = "Categorías Principales",
            icon = Icons.Default.Category,
            onSeeAllClick = onNavigateCatalog
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.categories) { category ->
                CategoryChip(
                    category = category,
                    onClick = { onCategoryClick(category.key) }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Featured Products Section
        SectionHeader(
            title = "Productos Destacados",
            icon = Icons.Default.Star,
            onSeeAllClick = onNavigateCatalog
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.featuredProducts.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { product ->
                        ProductCard(
                            product = product,
                            onClick = { onProductClick(product.slug) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    onSeeAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        TextButton(onClick = onSeeAllClick) {
            Text(
                text = "Ver todo",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun QuickActionChip(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun CategoryChip(
    category: ApiCategory,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
