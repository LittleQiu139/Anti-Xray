package org.example.spigot.antixray;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    private final Antixray plugin;

    public ReloadCommand(Antixray plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("axreload")) {
            plugin.reloadConfig(); // 重新加载配置文件
            sender.sendMessage("[AntiXray]重载成功.");
            return true;
        }
        return false;
    }
}

