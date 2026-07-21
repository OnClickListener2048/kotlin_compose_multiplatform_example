package org.example.project.repo

import com.watson.database.sqldelight.WatsonQueries
import org.example.project.chat.ProviderType
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ApiKeyInfo(
    val id: String,
    val providerType: ProviderType,
    val name: String,
    val apiKey: String,
    val baseUrl: String,
    val model: String,
    val isActive: Boolean,
    val createdAt: Long
)

class ApiKeyRepository(
    private val queries: WatsonQueries
) {
    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun now() = Clock.System.now().toEpochMilliseconds()

    fun getAllKeys(): List<ApiKeyInfo> {
        return queries.selectAllApiKeys().executeAsList().map { it.toApiKeyInfo() }
    }

    fun getKeysByProvider(providerType: ProviderType): List<ApiKeyInfo> {
        return queries.selectApiKeysByProvider(providerType).executeAsList().map { it.toApiKeyInfo() }
    }

    fun getActiveKey(): ApiKeyInfo? {
        return queries.selectActiveApiKey().executeAsOneOrNull()?.toApiKeyInfo()
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addKey(
        providerType: ProviderType,
        name: String,
        apiKey: String,
        baseUrl: String = providerType.defaultBaseUrl,
        model: String = providerType.defaultModel,
        setActive: Boolean = true
    ): ApiKeyInfo {
        val id = Uuid.random().toString()
        val time = now()

        if (setActive) {
            queries.deactivateAllApiKeys()
        }

        queries.insertApiKey(
            id = id,
            providerType = providerType,
            name = name,
            apiKey = apiKey,
            baseUrl = baseUrl.ifBlank { providerType.defaultBaseUrl },
            model = model.ifBlank { providerType.defaultModel },
            isActive = if (setActive) 1L else 0L,
            createdAt = time
        )

        return ApiKeyInfo(id, providerType, name, apiKey, baseUrl, model, setActive, time)
    }

    fun setActiveKey(id: String) {
        queries.deactivateAllApiKeys()
        queries.updateApiKeyActive(id = id, isActive = 1L)
    }

    fun deleteKey(id: String) {
        queries.deleteApiKey(id)
    }

    fun importKeys(keys: List<ApiKeyInfo>) {
        for (key in keys) {
            val existing = queries.selectApiKeyById(key.id).executeAsOneOrNull()
            if (existing == null) {
                queries.insertApiKey(
                    id = key.id,
                    providerType = key.providerType,
                    name = key.name,
                    apiKey = key.apiKey,
                    baseUrl = key.baseUrl,
                    model = key.model,
                    isActive = if (key.isActive) 1L else 0L,
                    createdAt = key.createdAt
                )
            }
        }
    }
}

private fun com.watson.database.sqldelight.ApiKey.toApiKeyInfo() = ApiKeyInfo(
    id = id,
    providerType = providerType,
    name = name,
    apiKey = apiKey,
    baseUrl = baseUrl,
    model = model,
    isActive = isActive != 0L,
    createdAt = createdAt
)
