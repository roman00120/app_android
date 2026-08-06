package com.totalground.app.ui.screens.viewer

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.totalground.app.ui.theme.TotalGroundNavy
import com.totalground.app.ui.theme.TotalGroundRed
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlbViewerScreen(
    productName: String,
    modelUrl: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    var activeModelNode by remember { mutableStateOf<ModelNode?>(null) }
    var activeSceneView by remember { mutableStateOf<SceneView?>(null) }

    val cleanAssetPath = remember(modelUrl) {
        modelUrl.removePrefix("file:///android_asset/").removePrefix("/")
    }

    DisposableEffect(modelUrl) {
        isLoading = true
        hasError = false
        onDispose {
            try {
                activeModelNode?.destroy()
                activeSceneView?.destroy()
                activeModelNode = null
                activeSceneView = null
            } catch (e: Exception) {
                Log.e("GlbViewerScreen", "Cleanup error: ${e.message}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Top Navigation Header
        Surface(
            color = TotalGroundNavy,
            shadowElevation = 4.dp
        ) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Visor 3D GLB Real",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = productName,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TotalGroundNavy)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // SceneView 3D Engine Native View
            AndroidView(
                factory = { ctx ->
                    SceneView(ctx).apply {
                        activeSceneView = this
                        cameraNode.position = Position(x = 0f, y = 0.2f, z = 2.2f)

                        scope.launch {
                            try {
                                Log.d("GlbViewerScreen", "PRODUCT_NAME: $productName | MODEL_URL: $modelUrl | LOADED_MODEL_PATH: $cleanAssetPath")
                                val modelInstance = modelLoader.createModelInstance(cleanAssetPath)
                                if (modelInstance != null) {
                                    val node = ModelNode(
                                        modelInstance = modelInstance,
                                        scaleToUnits = 1.0f,
                                        centerOrigin = Position(x = 0f, y = 0f, z = 0f)
                                    )

                                    addChildNode(node)
                                    activeModelNode = node
                                    isLoading = false
                                } else {
                                    isLoading = false
                                    hasError = true
                                }
                            } catch (e: Exception) {
                                Log.e("GlbViewerScreen", "Failed to load 3D GLB model: ${e.message}", e)
                                isLoading = false
                                hasError = true
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Loading Progress Overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = TotalGroundRed)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Renderizando modelo 3D GLB con SceneView / Filament...",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Error Overlay
            if (hasError) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewInAr,
                            contentDescription = null,
                            tint = TotalGroundRed,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No se pudo renderizar el modelo 3D GLB",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "El archivo 3D '$cleanAssetPath' requiere soporte de hardware OpenGL ES 3.0 en el emulador.",
                            fontSize = 13.sp,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onBackClick,
                            colors = ButtonDefaults.buttonColors(containerColor = TotalGroundRed)
                        ) {
                            Text("Regresar al Detalle")
                        }
                    }
                }
            }

            // Floating 3D Controls (Bottom)
            if (!isLoading && !hasError) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(20.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(TotalGroundNavy.copy(alpha = 0.9f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            activeModelNode?.rotation = Rotation(x = 0f, y = 0f, z = 0f)
                            activeSceneView?.cameraNode?.position = Position(x = 0f, y = 0.2f, z = 2.2f)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restablecer", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            activeSceneView?.cameraNode?.position = Position(x = 0f, y = 0f, z = 1.8f)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TotalGroundRed)
                    ) {
                        Icon(Icons.Default.CenterFocusStrong, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Centrar", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
