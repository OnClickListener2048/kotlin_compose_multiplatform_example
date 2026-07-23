package org.example.project.repo

import com.watson.database.sqldelight.WatsonQueries
import org.example.project.chat.ProviderType
import org.example.project.feature.user.CurrentUserProvider
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ApiKeyInfo(
    val id: String,
    val userId: String,
    val providerType: ProviderType,
    val name: String,
    val apiKey: String,
    val baseUrl: String,
    val model: String,
    val isActive: Boolean,
    val createdAt: Long
)

class ApiKeyRepository(
    private val queries: WatsonQueries,
    private val currentUser: CurrentUserProvider
) {
    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun now() = Clock.System.now().toEpochMilliseconds()

    fun getAllKeys(): List<ApiKeyInfo> {
        return queries.selectAllApiKeys(currentUser.currentUserId).executeAsList().map { it.toApiKeyInfo() }
    }

    fun getKeysByProvider(providerType: ProviderType): List<ApiKeyInfo> {
        return queries.selectApiKeysByProvider(currentUser.currentUserId, providerType).executeAsList().map { it.toApiKeyInfo() }
    }

    fun getActiveKey(): ApiKeyInfo? {
        return queries.selectActiveApiKey(currentUser.currentUserId).executeAsOneOrNull()?.toApiKeyInfo()
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
            queries.deactivateAllApiKeys(currentUser.currentUserId)
        }

        queries.insertApiKey(
            id = id,
            userId = currentUser.currentUserId,
            providerType = providerType,
            name = name,
            apiKey = apiKey,
            baseUrl = baseUrl.ifBlank { providerType.defaultBaseUrl },
            model = model.ifBlank { providerType.defaultModel },
            isActive = if (setActive) 1L else 0L,
            createdAt = time
        )

        return ApiKeyInfo(id, currentUser.currentUserId, providerType, name, apiKey, baseUrl, model, setActive, time)
    }

    fun setActiveKey(id: String) {
        queries.deactivateAllApiKeys(currentUser.currentUserId)
        queries.updateApiKeyActive(id = id, isActive = 1L, userId = currentUser.currentUserId)
    }

    fun deleteKey(id: String) {
        queries.deleteApiKey(id, currentUser.currentUserId)
    }

    fun importKeys(keys: List<ApiKeyInfo>) {
        for (key in keys) {
            val existing = queries.selectApiKeyById(key.id, currentUser.currentUserId).executeAsOneOrNull()
            if (existing == null) {
                queries.insertApiKey(
                    id = key.id,
                    userId = currentUser.currentUserId,
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
    userId = userId,
    providerType = providerType,
    name = name,
    apiKey = apiKey,
    baseUrl = baseUrl,
    model = model,
    isActive = isActive != 0L,
    createdAt = createdAt
)
