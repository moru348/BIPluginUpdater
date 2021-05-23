package dev.moru3.bipluginupdater.github

import okhttp3.Response

interface IAsset {
    val token: String
    val url: String
    val id: Int
    val nodeId: String
    val name: String
    val label: String
    val contentsType: String
    val state: String
    val size: Long
    val downloadCount: Int
    val createdAt: String
    val updatedAt: String
    val browserDownloadUrl: String

    fun download(): Response
}