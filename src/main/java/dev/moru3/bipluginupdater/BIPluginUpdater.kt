package dev.moru3.bipluginupdater

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin


class BIPluginUpdater : JavaPlugin() {
    var plugman = false

    override fun onEnable() {
        saveDefaultConfig()
        githubToken = config.getString("github.token")
        if(Bukkit.getPluginManager().isPluginEnabled("PlugMan")) {
            plugman = true
        }
        server.consoleSender.sendMessage("${this.name} is Enabled.")
        server.getPluginCommand("bipluginupdater")?.also {
            it.setExecutor(Command(this))
            it.tabCompleter = TabComplete()
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    var githubToken: String? = null
}