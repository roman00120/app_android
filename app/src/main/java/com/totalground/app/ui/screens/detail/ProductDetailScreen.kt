package com.totalground.app.ui.screens.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.totalground.app.data.model.ApiProductDetail
import com.totalground.app.ui.components.ErrorState
import com.totalground.app.ui.components.LoadingState
import com.totalground.app.ui.theme.TotalGroundNavy
import com.totalground.app.ui.theme.TotalGroundRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    slug: String,
    viewModel: ProductDetailViewModel,
    onBackClick: () -> Unit,
    onView3dClick: (String, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(slug) {
        viewModel.loadProductDetail(slug)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Back Navigation Bar
        Surface(
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Ficha Técnica",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TotalGroundNavy
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar al Catálogo",
                            tint = TotalGroundNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }

        when (val state = uiState) {
            is ProductDetailUiState.Loading -> LoadingState(message = "Cargando detalle del producto...")
            is ProductDetailUiState.Error -> ErrorState(message = state.message, onRetry = { viewModel.loadProductDetail(slug) })
            is ProductDetailUiState.Success -> DetailContent(
                product = state.product,
                onView3dClick = onView3dClick
            )
        }
    }
}

@Composable
private fun DetailContent(
    product: ApiProductDetail,
    onView3dClick: (String, String) -> Unit
) {
    val context = LocalContext.current
    val images = product.images
    var selectedImageUrl by remember {
        mutableStateOf(images.firstOrNull()?.url ?: product.icon ?: "")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Main Image Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = selectedImageUrl,
                contentDescription = product.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // 3D Badge on main image if 3D model exists
            if (!product.model3dUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TotalGroundRed)
                        .clickable { onView3dClick(product.name, product.model3dUrl) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ViewInAr,
                            contentDescription = "Modelo 3D",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "3D GLB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Image Gallery Thumbnails
        if (images.size > 1) {
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(images) { item ->
                    val isSelected = selectedImageUrl == item.url
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) TotalGroundRed else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedImageUrl = item.url }
                            .padding(4.dp)
                    ) {
                        AsyncImage(
                            model = item.url,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Label & Model Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            product.categoryLabel?.let { cat ->
                Text(
                    text = cat.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TotalGroundRed
                )
            }
            Text(
                text = "Modelo: ${product.model}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Product Name
        Text(
            text = product.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TotalGroundNavy,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Product Description
        if (!product.description.isNullOrBlank()) {
            Text(
                text = "Descripción",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TotalGroundNavy
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = product.description.orEmpty(),
                fontSize = 14.sp,
                color = Color(0xFF334155),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Features List
        if (product.features.isNotEmpty()) {
            Text(
                text = "Características Principales",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TotalGroundNavy
            )
            Spacer(modifier = Modifier.height(8.dp))
            product.features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = TotalGroundRed,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feature.title,
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B)
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Specifications Table
        if (product.specifications.isNotEmpty()) {
            Text(
                text = "Especificaciones Técnicas",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TotalGroundNavy
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    product.specifications.forEachIndexed { index, spec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (index % 2 == 0) Color(0xFFF8FAFC) else Color.White)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = spec.key,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TotalGroundNavy,
                                modifier = Modifier.weight(1.2f)
                            )
                            Text(
                                text = spec.value,
                                fontSize = 12.sp,
                                color = Color(0xFF475569),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 3D Model Button (ONLY SHOWN IF 3D MODEL EXISTS)
        if (!product.model3dUrl.isNullOrBlank()) {
            Button(
                onClick = { onView3dClick(product.name, product.model3dUrl) },
                colors = ButtonDefaults.buttonColors(containerColor = TotalGroundNavy),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ViewInAr,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ver Modelo 3D (GLB)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Datasheet PDF Button (ONLY SHOWN IF PDF EXISTS)
        if (!product.pdfUrl.isNullOrBlank()) {
            Button(
                onClick = { openPdf(context, product.pdfUrl) },
                colors = ButtonDefaults.buttonColors(containerColor = TotalGroundRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ver Ficha Técnica (PDF)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun openPdf(context: Context, pdfUrl: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(pdfUrl), "application/pdf")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No se encontró un lector PDF para abrir el documento.", Toast.LENGTH_LONG).show()
    }
}
