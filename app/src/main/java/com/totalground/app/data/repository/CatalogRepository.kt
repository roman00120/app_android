package com.totalground.app.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.totalground.app.data.model.ApiBanner
import com.totalground.app.data.model.ApiCategory
import com.totalground.app.data.model.ApiDocument
import com.totalground.app.data.model.ApiFeature
import com.totalground.app.data.model.ApiHomeResponse
import com.totalground.app.data.model.ApiMediaImage
import com.totalground.app.data.model.ApiMeta
import com.totalground.app.data.model.ApiProduct
import com.totalground.app.data.model.ApiProductDetail
import com.totalground.app.data.model.ApiResponse
import com.totalground.app.data.model.ApiSpecification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CatalogRepository(
    private val context: Context,
    private val gson: Gson = Gson()
) {
    private val tag = "CatalogRepository"

    @Volatile
    private var cachedProducts: List<ApiProduct>? = null

    @Volatile
    private var cachedProductDetails: MutableMap<String, ApiProductDetail>? = null

    @Volatile
    private var cachedCategories: List<ApiCategory>? = null

    private val categoryChildrenMap = mapOf(
        "tierras-fisicas" to setOf("tierras-kits", "tierras-electrodos", "tierras-compuestos", "tierras-conectores", "tierras-soldadura", "tierras-barras", "tierras-registros", "tierras-terrometro"),
        "pararrayos" to setOf("pararrayos-kits", "pararrayos-puntas", "pararrayos-mastiles", "pararrayos-accesorios", "pararrayos-contadores"),
        "supresores-transientes" to setOf("supresores-clase-c", "supresores-clase-b", "supresores-clase-a", "supresores-din-rail", "supresores-telcos"),
        "bancos-capacitores" to setOf("bancos-capacitores-productos", "bancos-fijos", "bancos-automaticos"),
        "telemetria-monitoreo" to setOf("telemetria-total-view", "telemetria-smart-view", "telemetria-terrometro", "telemetria-contador-descargas", "telemetria-nes", "telemetria-checktor", "telemetria-total-monitor", "telemetria-tgone"),
        "torres-arriostradas" to setOf()
    )

    private fun readAssetFile(filename: String): String {
        return try {
            context.assets.open(filename).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            context.assets.open("data/$filename").bufferedReader().use { it.readText() }
        }
    }

    private fun resolve3dAssetPath(rawPath: String?): String? {
        if (rawPath.isNullOrBlank()) return null
        var cleanPath = rawPath.trim()
            .removePrefix("/")
            .removePrefix("assets/")

        if (!cleanPath.startsWith("models/")) {
            cleanPath = "models/$cleanPath"
        }

        return try {
            context.assets.open(cleanPath).use { }
            Log.d(tag, "PRODUCT_3D_RESOLVED: asset path '$cleanPath' verified")
            "file:///android_asset/$cleanPath"
        } catch (e: Exception) {
            Log.w(tag, "PRODUCT_3D_MISSING: asset path '$cleanPath' missing in APK assets")
            null
        }
    }

    suspend fun getHomeData(language: String = "es"): Result<ApiHomeResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val categoriesResult = getCategories(language)
            val categories = categoriesResult.getOrDefault(emptyList())

            ensureProductsLoaded(language)
            val featured = cachedProducts?.take(6) ?: emptyList()

            ApiHomeResponse(
                locale = language,
                banners = listOf(
                    ApiBanner(
                        id = 1,
                        mediaType = "image",
                        mediaUrl = "file:///android_asset/banners/banner-1.webp",
                        altText = "Ingeniería en Protección y Calidad de Energía",
                        sortOrder = 1
                    )
                ),
                categories = categories,
                featuredProducts = featured
            )
        }.onFailure {
            Log.e(tag, "Error in getHomeData: ${it.message}", it)
        }
    }

    suspend fun getHome(language: String = "es"): Result<ApiHomeResponse> = getHomeData(language)

    suspend fun getCategories(language: String = "es"): Result<List<ApiCategory>> = withContext(Dispatchers.IO) {
        runCatching {
            if (cachedCategories != null) {
                return@runCatching cachedCategories!!
            }

            val jsonString = readAssetFile("catalogo-categorias.json")
            val rawArray = gson.fromJson(jsonString, Array<JsonObject>::class.java)

            val parsed = rawArray.mapIndexed { index, catObj ->
                val key = catObj.get("key")?.asString.orEmpty()
                val labelsObj = catObj.getAsJsonObject("labels")
                val name = getStringOrLocalized(labelsObj, language) ?: key

                val subcategories = mutableListOf<ApiCategory>()
                if (catObj.has("children") && catObj.get("children").isJsonArray) {
                    catObj.getAsJsonArray("children").forEachIndexed { subIndex, subElem ->
                        if (subElem.isJsonObject) {
                            val subObj = subElem.asJsonObject
                            val subKey = subObj.get("key")?.asString.orEmpty()
                            val subLabelsObj = subObj.getAsJsonObject("labels")
                            val subName = getStringOrLocalized(subLabelsObj, language) ?: subKey

                            subcategories.add(
                                ApiCategory(
                                    id = (index + 1) * 100 + subIndex + 1,
                                    key = subKey,
                                    name = subName,
                                    parentId = index + 1,
                                    sortOrder = subIndex + 1,
                                    isActive = true,
                                    subcategories = emptyList()
                                )
                            )
                        }
                    }
                }

                ApiCategory(
                    id = index + 1,
                    key = key,
                    name = name,
                    parentId = null,
                    sortOrder = index + 1,
                    isActive = true,
                    subcategories = subcategories
                )
            }

            cachedCategories = parsed
            parsed
        }.onFailure {
            Log.e(tag, "Error in getCategories: ${it.message}", it)
        }
    }

    suspend fun getProducts(
        categoryKey: String? = null,
        query: String? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
        page: Int = 1,
        language: String = "es"
    ): Result<ApiResponse<List<ApiProduct>>> = withContext(Dispatchers.IO) {
        runCatching {
            ensureProductsLoaded(language)

            var filtered = cachedProducts ?: emptyList()

            // 1. Category Filter by Stable Key / Subcategory Key
            if (!categoryKey.isNullOrBlank()) {
                val targetKey = categoryKey.trim().lowercase()
                if (targetKey != "all" && targetKey != "todos" && targetKey.isNotEmpty()) {
                    val childKeys = categoryChildrenMap[targetKey] ?: emptySet()

                    filtered = filtered.filter { p ->
                        val pCatKey = p.categoryKey?.trim()?.lowercase()
                        val pSubKey = p.subcategoryKey?.trim()?.lowercase()

                        pCatKey == targetKey ||
                        pSubKey == targetKey ||
                        (pCatKey != null && childKeys.contains(pCatKey)) ||
                        (pSubKey != null && childKeys.contains(pSubKey)) ||
                        (p.categoryLabel?.equals(targetKey, ignoreCase = true) == true) ||
                        (p.subcategoryLabel?.equals(targetKey, ignoreCase = true) == true)
                    }
                }
            }

            // 2. Search Query Filter
            val q = query?.trim()?.lowercase() ?: ""
            if (q.isNotEmpty()) {
                filtered = filtered.filter { p ->
                    p.name.lowercase().contains(q) ||
                    p.model.lowercase().contains(q) ||
                    p.slug.lowercase().contains(q) ||
                    (p.description?.lowercase()?.contains(q) == true) ||
                    (p.categoryLabel?.lowercase()?.contains(q) == true) ||
                    (p.subcategoryLabel?.lowercase()?.contains(q) == true) ||
                    (p.categoryKey?.lowercase()?.contains(q) == true) ||
                    (p.subcategoryKey?.lowercase()?.contains(q) == true)
                }
            }

            // 3. Sorting
            filtered = when (sortBy) {
                "name" -> filtered.sortedBy { it.name }
                "price" -> filtered.sortedBy { it.price ?: 0.0 }
                else -> filtered.sortedBy { it.sortOrder }
            }

            if (sortOrder?.equals("desc", ignoreCase = true) == true) {
                filtered = filtered.reversed()
            }

            val total = filtered.size

            ApiResponse(
                data = filtered,
                meta = ApiMeta(
                    currentPage = 1,
                    lastPage = 1,
                    perPage = total,
                    total = total
                )
            )
        }.onFailure {
            Log.e(tag, "Error in getProducts: ${it.message}", it)
        }
    }

    suspend fun getProductDetail(slug: String, language: String = "es"): Result<ApiProductDetail> = withContext(Dispatchers.IO) {
        runCatching {
            ensureProductsLoaded(language)

            val detail = cachedProductDetails?.get(slug)
                ?: cachedProductDetails?.values?.find { it.slug == slug || it.id.toString() == slug }
                ?: throw IllegalStateException("Producto '$slug' no encontrado en el catálogo local.")

            detail
        }.onFailure {
            Log.e(tag, "Error in getProductDetail ($slug): ${it.message}", it)
        }
    }

    suspend fun getDocuments(
        categoryKey: String? = null,
        query: String? = null,
        page: Int = 1,
        language: String = "es"
    ): Result<ApiResponse<List<ApiDocument>>> = withContext(Dispatchers.IO) {
        runCatching {
            val jsonString = readAssetFile("documents.json")
            val type = object : TypeToken<List<ApiDocument>>() {}.type
            var docs: List<ApiDocument> = gson.fromJson(jsonString, type) ?: emptyList()

            if (!categoryKey.isNullOrBlank()) {
                val targetKey = categoryKey.trim().lowercase()
                docs = docs.filter { it.categoryKey?.lowercase() == targetKey }
            }

            val q = query?.trim()?.lowercase() ?: ""
            if (q.isNotEmpty()) {
                docs = docs.filter { it.title.lowercase().contains(q) || (it.description?.lowercase()?.contains(q) == true) }
            }

            ApiResponse(
                data = docs,
                meta = ApiMeta(
                    currentPage = 1,
                    lastPage = 1,
                    perPage = docs.size,
                    total = docs.size
                )
            )
        }.onFailure {
            Log.e(tag, "Error in getDocuments: ${it.message}", it)
        }
    }

    @Synchronized
    private fun ensureProductsLoaded(language: String) {
        if (cachedProducts != null && cachedProductDetails != null) return

        val jsonString = readAssetFile("productos.json")
        val rawArray = gson.fromJson(jsonString, Array<JsonObject>::class.java)

        val productsList = mutableListOf<ApiProduct>()
        val detailsMap = mutableMapOf<String, ApiProductDetail>()

        rawArray.forEachIndexed { index, obj ->
            try {
                val id = obj.get("id")?.asInt ?: (index + 1)
                val slug = getStringOrLocalized(obj.get("slug"), language) ?: "producto-$id"

                val name = getStringOrLocalized(obj.get("nombre"), language)
                    ?: getStringOrLocalized(obj.get("name"), language)
                    ?: "Producto $id"

                val description = getStringOrLocalized(obj.get("descripcion"), language)

                val categoryKey = obj.get("category")?.asString?.trim()
                val subcategoryKey = obj.get("subcategory")?.asString?.trim()

                val categoryLabel = getStringOrLocalized(obj.get("categoria"), language)
                    ?: categoryKey?.replace("-", " ")

                val subcategoryLabel = getStringOrLocalized(obj.get("subcategoria"), language)
                    ?: subcategoryKey?.replace("-", " ")

                val model = getStringOrLocalized(obj.get("modelo"), language).orEmpty().ifBlank { "TG-$id" }
                val family = getStringOrLocalized(obj.get("familia"), language)

                // Images
                val rawImages = obj.get("imagenes")
                val mediaImages = mutableListOf<ApiMediaImage>()

                if (rawImages != null && rawImages.isJsonArray) {
                    rawImages.asJsonArray.forEachIndexed { imgIdx, elem ->
                        val rawPath = getStringOrLocalized(elem, language)
                        if (!rawPath.isNullOrBlank()) {
                            val assetPath = transformToAssetUrl(rawPath)
                            mediaImages.add(
                                ApiMediaImage(
                                    id = imgIdx + 1,
                                    url = assetPath,
                                    label = if (imgIdx == 0) "Principal" else "Vista ${imgIdx + 1}",
                                    isPrimary = imgIdx == 0
                                )
                            )
                        }
                    }
                }

                val primaryImageUrl = mediaImages.firstOrNull()?.url
                    ?: transformToAssetUrl(getStringOrLocalized(obj.get("image"), language))

                // Features
                val featuresList = getFeaturesList(obj.get("caracteristicas"), language)

                // Specifications
                val specsList = getSpecificationsList(obj, language)

                // PDF
                val pdfPath = getStringOrLocalized(obj.get("pdf"), language)
                val pdfUrl = if (!pdfPath.isNullOrBlank()) transformToAssetUrl(pdfPath) else null

                // 3D Model GLB - Read strictly from productos.json (has3D, model3d, modelo3d)
                val has3D = obj.get("has3D")?.asBoolean == true || obj.get("has3d")?.asBoolean == true
                val rawModel3d = getStringOrLocalized(obj.get("model3d"), language)
                    ?: getStringOrLocalized(obj.get("modelo3d"), language)
                    ?: getStringOrLocalized(obj.get("glbFile"), language)

                val resolved3dPath = if (has3D && !rawModel3d.isNullOrBlank()) {
                    resolve3dAssetPath(rawModel3d)
                } else {
                    null
                }

                Log.d(tag, "PRODUCT_3D_AUDIT: ID=$id | SLUG=$slug | HAS3D=$has3D | RAW_PATH=$rawModel3d | RESOLVED=$resolved3dPath")

                val apiProduct = ApiProduct(
                    id = id,
                    model = model,
                    family = family,
                    name = name,
                    slug = slug,
                    description = description,
                    price = null,
                    icon = primaryImageUrl,
                    image = primaryImageUrl,
                    categoryKey = categoryKey,
                    subcategoryKey = subcategoryKey,
                    categoryLabel = categoryLabel,
                    subcategoryLabel = subcategoryLabel,
                    has3D = has3D && resolved3dPath != null,
                    model3d = rawModel3d,
                    modelo3d = rawModel3d,
                    model3dUrl = resolved3dPath,
                    arEnabled = resolved3dPath != null,
                    sortOrder = index + 1
                )

                val apiDetail = ApiProductDetail(
                    id = id,
                    model = model,
                    family = family,
                    name = name,
                    slug = slug,
                    description = description,
                    price = null,
                    icon = primaryImageUrl,
                    categoryKey = categoryKey,
                    subcategoryKey = subcategoryKey,
                    categoryLabel = categoryLabel,
                    subcategoryLabel = subcategoryLabel,
                    has3D = has3D && resolved3dPath != null,
                    model3d = rawModel3d,
                    modelo3d = rawModel3d,
                    arEnabled = resolved3dPath != null,
                    metaTitle = "$name | Total Ground",
                    metaDescription = description,
                    images = mediaImages,
                    model3dUrl = resolved3dPath,
                    pdfUrl = pdfUrl,
                    features = featuresList,
                    specifications = specsList,
                    categories = emptyList()
                )

                productsList.add(apiProduct)
                detailsMap[slug] = apiDetail
            } catch (e: Exception) {
                Log.e(tag, "Failed to parse product at index $index: ${e.message}", e)
            }
        }

        cachedProducts = productsList
        cachedProductDetails = detailsMap
    }

    private fun transformToAssetUrl(path: String?): String {
        if (path.isNullOrBlank()) return "file:///android_asset/products/default.webp"
        if (path.startsWith("file:///")) return path
        val cleanPath = path.removePrefix("/").removePrefix("assets/")
        return "file:///android_asset/$cleanPath"
    }

    private fun getStringOrLocalized(element: JsonElement?, language: String): String? {
        if (element == null || element.isJsonNull) return null
        if (element.isJsonPrimitive) return element.asString

        if (element.isJsonObject) {
            val obj = element.asJsonObject
            if (obj.has(language) && !obj.get(language).isJsonNull) {
                return obj.get(language).asString
            }
            if (obj.has("es") && !obj.get("es").isJsonNull) {
                return obj.get("es").asString
            }
            if (obj.has("en") && !obj.get("en").isJsonNull) {
                return obj.get("en").asString
            }
        }
        return null
    }

    private fun getFeaturesList(element: JsonElement?, language: String): List<ApiFeature> {
        val list = mutableListOf<ApiFeature>()
        if (element == null || element.isJsonNull) return list

        val array = when {
            element.isJsonArray -> element.asJsonArray
            element.isJsonObject -> {
                val obj = element.asJsonObject
                when {
                    obj.has(language) && obj.get(language).isJsonArray -> obj.getAsJsonArray(language)
                    obj.has("es") && obj.get("es").isJsonArray -> obj.getAsJsonArray("es")
                    else -> null
                }
            }
            else -> null
        }

        array?.forEachIndexed { index, item ->
            val title = getStringOrLocalized(item, language) ?: item.toString()
            if (title.isNotBlank()) {
                list.add(ApiFeature(id = index + 1, title = title))
            }
        }

        return list
    }

    private fun getSpecificationsList(obj: JsonObject, language: String): List<ApiSpecification> {
        val list = mutableListOf<ApiSpecification>()
        val tablaElem = obj.get("tabla") ?: return list

        val array = when {
            tablaElem.isJsonArray -> tablaElem.asJsonArray
            tablaElem.isJsonObject -> {
                val tObj = tablaElem.asJsonObject
                when {
                    tObj.has(language) && tObj.get(language).isJsonArray -> tObj.getAsJsonArray(language)
                    tObj.has("es") && tObj.get("es").isJsonArray -> tObj.getAsJsonArray("es")
                    else -> null
                }
            }
            else -> null
        }

        array?.forEachIndexed { index, item ->
            if (item.isJsonObject) {
                val itemObj = item.asJsonObject
                val key = getStringOrLocalized(itemObj.get("clave"), language)
                    ?: getStringOrLocalized(itemObj.get("key"), language)
                    ?: getStringOrLocalized(itemObj.get("parametro"), language)
                    ?: "Especificación ${index + 1}"

                val value = getStringOrLocalized(itemObj.get("valor"), language)
                    ?: getStringOrLocalized(itemObj.get("value"), language)
                    ?: ""

                if (key.isNotBlank()) {
                    list.add(ApiSpecification(id = index + 1, key = key, value = value))
                }
            }
        }

        return list
    }
}
