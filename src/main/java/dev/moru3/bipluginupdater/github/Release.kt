package dev.moru3.bipluginupdater.github

/**
 * 多分完璧ではないけど使える程度にはなってると思います。
 */
class Release(
    override val url: String,
    override val assetsUrl: String,
    override val uploadUrl: String,
    override val htmlUrl: String,
    override val id: Int,
    override val nodeId: String,
    override val tagName: String,
    override val targetCommitish: String,
    override val name: String,
    override val draft: Boolean,
    override val preRelease: Boolean,
    override val createdAt: String,
    override val publishesAt: String,
    override val assets: List<Asset>,
    override val tarballUrl: String,
    override val zipballUrl: String,
    override val body: String,
): IRelease