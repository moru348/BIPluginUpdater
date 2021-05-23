package dev.moru3.bipluginupdater

import dev.moru3.bipluginupdater.github.GitHubReleaseUtil
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileOutputStream


class BIPluginUpdater : JavaPlugin() {
    private var plugman = false

    override fun onEnable() {
        saveDefaultConfig()
        githubToken = config.getString("github.token")
        if(Bukkit.getPluginManager().isPluginEnabled("PlugMan")) {
            plugman = true
        }
        this.server.consoleSender.sendMessage("${this.name} is Enabled.")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private var githubToken: String? = null

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(args.isEmpty()) { return false }
        try {
            when (args[0]) {
                "set" -> {
                    if (args.size == 2) {
                        return false
                    }
                    when (args[1]) {
                        "token" -> {
                            if (args.size == 3) {
                                return false
                            }
                            githubToken = args[2]
                            config.set("token", githubToken)
                            saveConfig()
                        }
                    }
                }
                "update" -> {
                    if (args.size <= 1) {
                        throw IllegalArgumentException("引数が足りません。")
                    }
                    when (args[1]) {
                        "github" -> {
                            /**
                             * args[0] = update
                             * args[1] = github
                             * args[2] = user
                             * args[3] = repo
                             * args[4] = plugin
                             */
                            if (args.size <= 4) {
                                throw IllegalArgumentException("パラメータが不正です。")
                            }
                            val plugin = Bukkit.getPluginManager().getPlugin(args[4]) ?: throw IllegalArgumentException(
                                "プラグインが見つかりません。"
                            )
                            if (plugin !is JavaPlugin) {
                                throw IllegalArgumentException("エラーが発生しました。")
                            }
                            val file = plugin::class.java.getDeclaredField("file").get(plugin)
                            if (file !is File) {
                                throw IllegalArgumentException("とても想定外なエラーが発生しました。")
                            }
                            val releases = GitHubReleaseUtil(args[2], args[3], githubToken ?: throw IllegalArgumentException("tokenが設定されていません。"), true)
                            if (releases.releases.isEmpty()) {
                                throw IllegalArgumentException("${releases.url}にはリリースが存在しません。")
                            }
                            val release = releases.releases[0]
                            if (release.assets.isEmpty()) {
                                throw IllegalArgumentException("assetsが存在しません。")
                            }
                            val asset = release.assets[0]
                            val response = asset.download()
                            val inputStream = response.body?.byteStream() ?: throw IllegalArgumentException("想定外のレスポンスが返ってきました。")
                            val buf = ByteArray(4096)
                            var readSize: Int
                            var total = 0
                            if (plugman) {
                                val pluginUtil = Class.forName("com.rylinaux.plugman.util.PluginUtil")
                                val unload = pluginUtil.getMethod("unload", Plugin::class.java)
                                unload.invoke(null, plugin)
                            } else {
                                Bukkit.getPluginManager().disablePlugin(plugin)
                            }
                            file.delete()
                            val fos = FileOutputStream(this.file.parentFile.resolve(asset.name))
                            while (inputStream.read(buf).also { readSize = it } == -1) {
                                total += readSize
                                fos.write(buf, 0, readSize)
                            }
                            fos.flush()
                            fos.close()
                            inputStream.close()
                            if (plugman) {
                                val pluginUtil = Class.forName("com.rylinaux.plugman.util.PluginUtil")
                                val load = pluginUtil.getMethod("unload", String::class.java)
                                load.invoke(null, asset.name)
                            } else {
                                server.reload()
                            }
                        }
                    }
                }
            }
        } catch (e: IllegalArgumentException) {
            sender.sendMessage(e.message?:"YO!YO!ですよ!")
        }
        return true
    }
}