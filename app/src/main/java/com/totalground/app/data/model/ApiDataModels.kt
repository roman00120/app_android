package com.totalground.app.data.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("data") val data: T? = null,
    @SerializedName("links") val links: ApiLinks? = null,
    @SerializedName("meta") val meta: ApiMeta? = null
)

data class ApiHomeResponse(
    @SerializedName("locale") val locale: String = "es",
    @SerializedName("banners") val banners: List<ApiBanner> = emptyList(),
    @SerializedName("categories") val categories: List<ApiCategory> = emptyList(),
    @SerializedName("featured_products") val featuredProducts: List<ApiProduct> = emptyList()
)

data class ApiBanner(
    @SerializedName("id") val id: Int,
    @SerializedName("media_type") val mediaType: String,
    @SerializedName("media_url") val mediaUrl: String,
    @SerializedName("alt_text") val altText: String? = null,
    @SerializedName("link_url") val linkUrl: String? = null,
    @SerializedName("sort_order") val sortOrder: Int = 0,
    @SerializedName("is_active") val isActive: Boolean = true
)

data class ApiCategory(
    @SerializedName("id") val id: Int,
    @SerializedName("key") val key: String,
    @SerializedName("name") val name: String,
    @SerializedName("parent_id") val parentId: Int? = null,
    @SerializedName("sort_order") val sortOrder: Int = 0,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("subcategories") val subcategories: List<ApiCategory> = emptyList()
)

data class ApiProduct(
    @SerializedName("id") val id: Int,
    @SerializedName("model") val model: String,
    @SerializedName("family") val family: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("category_key") val categoryKey: String? = null,
    @SerializedName("subcategory_key") val subcategoryKey: String? = null,
    @SerializedName("category_label") val categoryLabel: String? = null,
    @SerializedName("subcategory_label") val subcategoryLabel: String? = null,
    @SerializedName("has3D") val has3D: Boolean = false,
    @SerializedName("model3d") val model3d: String? = null,
    @SerializedName("modelo3d") val modelo3d: String? = null,
    @SerializedName("model_3d_url") val model3dUrl: String? = null,
    @SerializedName("ar_enabled") val arEnabled: Boolean = false,
    @SerializedName("sort_order") val sortOrder: Int = 0
)

data class ApiProductDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("model") val model: String,
    @SerializedName("family") val family: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("category_key") val categoryKey: String? = null,
    @SerializedName("subcategory_key") val subcategoryKey: String? = null,
    @SerializedName("category_label") val categoryLabel: String? = null,
    @SerializedName("subcategory_label") val subcategoryLabel: String? = null,
    @SerializedName("has3D") val has3D: Boolean = false,
    @SerializedName("model3d") val model3d: String? = null,
    @SerializedName("modelo3d") val modelo3d: String? = null,
    @SerializedName("ar_enabled") val arEnabled: Boolean = false,
    @SerializedName("meta_title") val metaTitle: String? = null,
    @SerializedName("meta_description") val metaDescription: String? = null,
    @SerializedName("images") val images: List<ApiMediaImage> = emptyList(),
    @SerializedName("model_3d_url") val model3dUrl: String? = null,
    @SerializedName("pdf_url") val pdfUrl: String? = null,
    @SerializedName("features") val features: List<ApiFeature> = emptyList(),
    @SerializedName("specifications") val specifications: List<ApiSpecification> = emptyList(),
    @SerializedName("categories") val categories: List<ApiCategory> = emptyList()
)

data class ApiMediaImage(
    @SerializedName("id") val id: Int,
    @SerializedName("url") val url: String,
    @SerializedName("label") val label: String? = null,
    @SerializedName("is_primary") val isPrimary: Boolean = false
)

data class ApiFeature(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null
)

data class ApiSpecification(
    @SerializedName("id") val id: Int,
    @SerializedName("key") val key: String,
    @SerializedName("value") val value: String
)

data class ApiDocument(
    @SerializedName("id") val id: Int,
    @SerializedName("key") val key: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("download_url") val downloadUrl: String? = null,
    @SerializedName("cover_url") val coverUrl: String? = null,
    @SerializedName("category_key") val categoryKey: String? = null,
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("commercial_category") val commercialCategory: String? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("file_type") val fileType: String? = null,
    @SerializedName("size_bytes") val sizeBytes: Long? = null,
    @SerializedName("size_formatted") val sizeFormatted: String? = null,
    @SerializedName("tags") val tags: List<String> = emptyList(),
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class ApiLinks(
    @SerializedName("first") val first: String? = null,
    @SerializedName("last") val last: String? = null,
    @SerializedName("prev") val prev: String? = null,
    @SerializedName("next") val next: String? = null
)

data class ApiMeta(
    @SerializedName("current_page") val currentPage: Int = 1,
    @SerializedName("from") val from: Int? = null,
    @SerializedName("last_page") val lastPage: Int = 1,
    @SerializedName("per_page") val perPage: Int = 1000,
    @SerializedName("to") val to: Int? = null,
    @SerializedName("total") val total: Int = 0
)
