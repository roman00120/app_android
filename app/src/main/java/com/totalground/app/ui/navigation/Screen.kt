package com.totalground.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Catalog : Screen("catalog?categoryKey={categoryKey}") {
        fun createRoute(categoryKey: String? = null): String {
            return if (categoryKey.isNullOrBlank()) "catalog" else "catalog?categoryKey=$categoryKey"
        }
    }
    object ProductDetail : Screen("product/{slug}") {
        fun createRoute(slug: String): String = "product/$slug"
    }
    object GlbViewer : Screen("viewer3d?productName={productName}&modelUrl={modelUrl}") {
        fun createRoute(productName: String, modelUrl: String): String {
            val encodedName = java.net.URLEncoder.encode(productName, "UTF-8")
            val encodedUrl = java.net.URLEncoder.encode(modelUrl, "UTF-8")
            return "viewer3d?productName=$encodedName&modelUrl=$encodedUrl"
        }
    }
}
