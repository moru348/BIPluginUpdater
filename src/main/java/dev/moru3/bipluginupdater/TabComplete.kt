package dev.moru3.bipluginupdater

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.plugin.Plugin

class TabComplete: TabCompleter {
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        val args1 = mutableListOf("set", "update")
        if(args.isEmpty()) {
            return args1
        } else if(args.size==1) {
            return args1.filter { it.startsWith(args[0]) }.toMutableList()
        } else if(args.size==2) {
            when(args[0]) {
                "set" -> {
                    return mutableListOf("token").filter { it.startsWith(args[1]) }.toMutableList()
                }
                "update" -> {
                    return mutableListOf("github").filter { it.startsWith(args[1]) }.toMutableList()
                }
            }
        } else if(args.size==3) {
            when(args[0]) {
                "update" -> {
                    when(args[1]) {
                        "github" -> {
                            return Bukkit.getPluginManager().plugins.map(Plugin::getName).filter { it.startsWith(args[2]) }.toMutableList()
                        }
                    }
                }
            }
        }
        return mutableListOf()
    }
}