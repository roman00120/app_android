package com.totalground.app.data.api

import com.totalground.app.data.model.ApiBanner
import com.totalground.app.data.model.ApiCategory
import com.totalground.app.data.model.ApiDocument
import com.totalground.app.data.model.ApiHomeResponse
import com.totalground.app.data.model.ApiProduct
import com.totalground.app.data.model.ApiProductDetail
import com.totalground.app.data.model.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface TotalGroundApiService {

    @GET("api/v1/home")
    suspend fun getHome(
        @Header("Accept-Language") language: String = "es"
    ): ApiHomeResponse

    @GET("api/v1/banners")
    suspend fun getBanners(
        @Header("Accept-Language") language: String = "es"
    ): ApiResponse<List<ApiBanner>>

    @GET("api/v1/categories")
    suspend fun getCategories(
        @Header("Accept-Language") language: String = "es"
    ): ApiResponse<List<ApiCategory>>

    @GET("api/v1/products")
    suspend fun getProducts(
        @Header("Accept-Language") language: String = "es",
        @Query("cat") categoryKey: String? = null,
        @Query("q") query: String? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): ApiResponse<List<ApiProduct>>

    @GET("api/v1/products/{slug}")
    suspend fun getProductBySlug(
        @Path("slug") slug: String,
        @Header("Accept-Language") language: String = "es"
    ): ApiResponse<ApiProductDetail>

    @GET("api/v1/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Header("Accept-Language") language: String = "es",
        @Query("page") page: Int = 1
    ): ApiResponse<List<ApiProduct>>

    @GET("api/v1/documents")
    suspend fun getDocuments(
        @Header("Accept-Language") language: String = "es",
        @Query("cat") categoryKey: String? = null,
        @Query("q") query: String? = null,
        @Query("page") page: Int = 1
    ): ApiResponse<List<ApiDocument>>
}
