package dev.moru3.bipluginupdater.github

interface IRelease {
    val url: String
    val assetsUrl: String
    val uploadUrl: String
    val htmlUrl: String
    val id: Int
    val nodeId: String
    val tagName: String
    val targetCommitish: String
    val name: String
    val draft: Boolean
    val preRelease: Boolean
    val createdAt: String
    val publishesAt: String
    val assets: List<Asset>
    val tarballUrl: String
    val zipballUrl: String
    val body: String
}