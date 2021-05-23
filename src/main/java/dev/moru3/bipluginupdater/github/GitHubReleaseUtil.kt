package dev.moru3.bipluginupdater.github

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request

class GitHubReleaseUtil(val user: String, val repo: String, val token: String, val latestOnly: Boolean = false) {
    var url = "https://github.com/$user/$repo/releases"
    val releases = mutableListOf<IRelease>()

    init {
        if(latestOnly) { url += "/latest" }

        val request = Request.Builder().url(url)
            .addHeader("Authorization", "token $token").get().build()
        val response = OkHttpClient().newCall(request).execute()
        when(response.code) {
            401 -> {
                throw IllegalArgumentException("Tokenの認証に失敗しました。")
            }
            404 -> {
                throw IllegalArgumentException("URLが間違っている、リリース存在しない、もしくはreleasesにプレリリースしかありません。")
            }
            200 -> {
                val json = Gson().fromJson(response.body.toString(), JsonObject::class.java)
                if(json.size()==0) { throw IllegalArgumentException("$url にはリリースがありません。") }
                json.asJsonArray.forEach {
                    val obj = it.asJsonObject
                    val assets = mutableListOf<Asset>()
                    obj.get("assets").asJsonArray.forEach {
                        val objc = it.asJsonObject
                        val asset = Asset(
                            token = token,
                            url = objc.get("url").asString,
                            id = objc.get("id").asInt,
                            nodeId = objc.get("node_id").asString,
                            name = objc.get("name").asString,
                            label = objc.get("label").asString,
                            contentsType = objc.get("content_type").asString,
                            state = objc.get("state").asString,
                            size = objc.get("size").asLong,
                            downloadCount = objc.get("download_count").asInt,
                            createdAt = objc.get("created_at").asString,
                            updatedAt = objc.get("updated_at").asString,
                            browserDownloadUrl = objc.get("browser_download_url").asString
                        )
                        assets.add(asset)
                    }
                    val release = Release(
                        url = obj.get("url").asString,
                        assetsUrl = obj.get("assets_url").asString,
                        uploadUrl = obj.get("upload_url").asString,
                        htmlUrl = obj.get("html_url").asString,
                        id = obj.get("id").asInt,
                        nodeId = obj.get("node_id").asString,
                        tagName = obj.get("tag_name").asString,
                        targetCommitish = obj.get("target_committish").asString,
                        name = obj.get("name").asString,
                        draft = obj.get("draft").asBoolean,
                        preRelease = obj.get("prerelease").asBoolean,
                        createdAt = obj.get("created_at").asString,
                        publishesAt = obj.get("publishes_at").asString,
                        assets = assets,
                        tarballUrl = obj.get("tarball_url").asString,
                        zipballUrl = obj.get("zipball_url").asString,
                        body = obj.get("body").asString
                    )
                    releases.add(release)
                }
            }
            else -> {
                throw IllegalArgumentException("${response.code} エラーが発生しました。")
            }
        }
    }
}