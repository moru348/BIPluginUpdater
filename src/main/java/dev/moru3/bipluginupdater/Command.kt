package dev.moru3.bipluginupdater

import dev.moru3.bipluginupdater.github.GitHubReleaseUtil
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Command(private val main: BIPluginUpdater): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(args.isEmpty()) { return false }
        try {
            when (args[0]) {
                "set" -> {
                    if (args.size <=1) {
                        throw IllegalArgumentException("引数おかしいよ。")
                    }
                    when (args[1]) {
                        "token" -> {
                            if (args.size <= 2) {
                                throw IllegalArgumentException("引数が足りません。")
                            }
                            sender.sendMessage("tokenが設定されました。")
                            main.githubToken = args[2]
                            main.config.set("github.token", main.githubToken)
                            main.saveConfig()
                        }
                        else -> {
                            throw IllegalArgumentException("引数おかしいよ。")
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
                            val plugin = Bukkit.getPluginManager().getPlugin(args[2]) ?: throw IllegalArgumentException(
                                "プラグインが見つかりません。"
                            )
                            if (plugin !is JavaPlugin) {
                                throw IllegalArgumentException("エラーが発生しました。")
                            }
                            val file = JavaPlugin::class.java.getDeclaredMethod("getFile").also {
                                it.isAccessible = true
                            }.invoke(plugin)
                            if (file !is File) {
                                throw IllegalArgumentException("とても想定外なエラーが発生しました。")
                            }
                            val releases = GitHubReleaseUtil(args[3], args[4], main.githubToken ?: throw IllegalArgumentException("tokenが設定されていません。"), true)
                            if (releases.releases.isEmpty()) {
                                throw IllegalArgumentException("${releases.url}にはリリースが存在しません。")
                            }
                            val release = releases.releases[0]
                            if (release.assets.isEmpty()) {
                                throw IllegalArgumentException("assetsが存在しません。")
                            }
                            val asset = release.assets[0]
                            sender.sendMessage("downloading...")
                            val response = asset.download()
                            val inputStream = response.body?.byteStream() ?: throw IllegalArgumentException("想定外のレスポンスが返ってきました。")
                            val buf = ByteArray(1024)
                            if (main.plugman) {
                                try {
                                    val pluginUtil = Class.forName("com.rylinaux.plugman.util.PluginUtil")
                                    val unload = pluginUtil.getMethod("unload", Plugin::class.java)
                                    unload.invoke(null, plugin)
                                } catch (e: Exception) {
                                    Bukkit.getPluginManager().disablePlugin(plugin)
                                }
                            } else {
                                Bukkit.getPluginManager().disablePlugin(plugin)
                            }
                            try {
                                val isWindows = System.getProperty("os.name").startsWith("Windows")
                                if(isWindows) {
                                    sender.sendMessage("使用中のOSがWindows(www)のためファイルを ${file.name} にリネームします。")
                                }
                                val outputStream = BufferedOutputStream(FileOutputStream(if(isWindows) file else File("plugins").resolve(asset.name)))
                                var count: Int
                                while (inputStream.read(buf).also { count = it } != -1) {
                                    outputStream.write(buf, 0, count)
                                }
                                outputStream.flush()
                                outputStream.close()
                                inputStream.close()
                                if (main.plugman) {
                                    try {
                                        val pluginUtil = Class.forName("com.rylinaux.plugman.util.PluginUtil")
                                        val load = pluginUtil.getMethod("unload", String::class.java)
                                        load.invoke(null, asset.name)
                                    } catch (e: Exception) {
                                        main.server.reload()
                                    }
                                } else {
                                    main.server.reload()
                                }
                                sender.sendMessage("操作が完了しました。")
                            } catch (e: IOException) {
                                throw IllegalArgumentException("ファイルの保存中にエラーが発生しました。")
                            }
                        }
                        else -> {
                            throw IllegalArgumentException("引数がおかしいよ。")
                        }
                    }
                }
                else -> {
                    throw IllegalArgumentException("引数がおかしいよ。")
                }
            }
        } catch (e: IllegalArgumentException) {
            sender.sendMessage(e.message?:"error")
        }
        return true
    }
}