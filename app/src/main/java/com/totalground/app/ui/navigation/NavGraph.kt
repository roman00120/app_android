package com.totalground.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.totalground.app.data.repository.CatalogRepository
import com.totalground.app.ui.components.TopNavBar
import com.totalground.app.ui.screens.catalog.CatalogScreen
import com.totalground.app.ui.screens.catalog.CatalogViewModel
import com.totalground.app.ui.screens.detail.ProductDetailScreen
import com.totalground.app.ui.screens.detail.ProductDetailViewModel
import com.totalground.app.ui.screens.splash.SplashScreen
import com.totalground.app.ui.screens.viewer.GlbViewerScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current.applicationContext
    val repository = remember { CatalogRepository(context) }

    val catalogViewModel = remember { CatalogViewModel(repository) }
    val productDetailViewModel = remember { ProductDetailViewModel(repository) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Splash.route
    val isSplashScreen = currentRoute == Screen.Splash.route
    val isDetailScreen = currentRoute.startsWith("product/")
    val isViewerScreen = currentRoute.startsWith("viewer3d")

    Scaffold(
        topBar = {
            if (!isSplashScreen && !isDetailScreen && !isViewerScreen) {
                TopNavBar()
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.Catalog.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.Catalog.route,
                arguments = listOf(
                    navArgument("categoryKey") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val categoryKey = backStackEntry.arguments?.getString("categoryKey")
                CatalogScreen(
                    viewModel = catalogViewModel,
                    initialCategoryKey = categoryKey,
                    onProductClick = { slug ->
                        navController.navigate(Screen.ProductDetail.createRoute(slug))
                    }
                )
            }

            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(
                    navArgument("slug") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val slug = backStackEntry.arguments?.getString("slug") ?: ""
                ProductDetailScreen(
                    slug = slug,
                    viewModel = productDetailViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onView3dClick = { name, url ->
                        navController.navigate(Screen.GlbViewer.createRoute(name, url))
                    }
                )
            }

            composable(
                route = Screen.GlbViewer.route,
                arguments = listOf(
                    navArgument("productName") { type = NavType.StringType; defaultValue = "Producto 3D" },
                    navArgument("modelUrl") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val rawName = backStackEntry.arguments?.getString("productName") ?: "Producto 3D"
                val rawUrl = backStackEntry.arguments?.getString("modelUrl") ?: ""
                val decodedName = java.net.URLDecoder.decode(rawName, "UTF-8")
                val decodedUrl = java.net.URLDecoder.decode(rawUrl, "UTF-8")

                GlbViewerScreen(
                    productName = decodedName,
                    modelUrl = decodedUrl,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
