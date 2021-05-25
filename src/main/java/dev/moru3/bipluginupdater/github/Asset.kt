package dev.moru3.bipluginupdater.github

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class Asset(
    override val token: String,
    override val url: String,
    override val id: Int,
    override val nodeId: String,
    override val name: String,
    override val label: String?,
    override val contentsType: String,
    override val state: String,
    override val size: Long,
    override val downloadCount: Int,
    override val createdAt: String,
    override val updatedAt: String,
    override val browserDownloadUrl: String
): IAsset {
    override fun download(): Response {
        val fileRequest = Request.Builder().url(url)
            .addHeader("Accept", "application/octet-stream")
            .addHeader("Authorization", "token $token").build()
        return OkHttpClient().newCall(fileRequest).execute()
    }
}