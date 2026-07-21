package org.example.project.feature.files

import com.watson.database.sqldelight.WatsonQueries
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class FileAsset(
    val id: String,
    val workspaceId: String?,
    val conversationId: String?,
    val displayName: String,
    val mimeType: String,
    val localPath: String,
    val sizeBytes: Long,
    val createdAt: Long
)

class FileAssetRepository(private val queries: WatsonQueries) {
    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun now() = Clock.System.now().toEpochMilliseconds()

    fun forConversation(conversationId: String): List<FileAsset> =
        queries.selectFilesForConversation(conversationId).executeAsList().map { it.toFileAsset() }

    @OptIn(ExperimentalUuidApi::class)
    fun attach(
        displayName: String,
        mimeType: String,
        localPath: String,
        sizeBytes: Long,
        workspaceId: String?,
        conversationId: String?
    ): FileAsset {
        val asset = FileAsset(Uuid.random().toString(), workspaceId, conversationId, displayName, mimeType, localPath, sizeBytes, now())
        queries.insertFileAsset(asset.id, asset.workspaceId, asset.conversationId, asset.displayName, asset.mimeType, asset.localPath, asset.sizeBytes, asset.createdAt)
        return asset
    }

    fun delete(id: String) = queries.deleteFileAsset(id)
}

private fun com.watson.database.sqldelight.FileAsset.toFileAsset() = FileAsset(
    id = id,
    workspaceId = workspaceId,
    conversationId = conversationId,
    displayName = displayName,
    mimeType = mimeType,
    localPath = localPath,
    sizeBytes = sizeBytes,
    createdAt = createdAt
)
