package org.example.spigot.antixray;

import it.unimi.dsi.fastutil.Hash;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;

public class Antixray extends JavaPlugin {
    public static HashMap<Material,Integer> stoneLimits = new HashMap<>();

    @Override
    public void onEnable() {
        this.getCommand("axreload").setExecutor(new ReloadCommand(this));
        this.saveDefaultConfig();  // 确保配置文件被加载和保存
        getServer().getPluginManager().registerEvents(new StoneMiningListener(this), this);
        stoneLimits.put(Material.STONE,2);
        stoneLimits.put(Material.NETHERRACK,5);
        stoneLimits.put(Material.GRANITE,2);
        stoneLimits.put(Material.DIORITE,2);
        stoneLimits.put(Material.ANDESITE,2);
        stoneLimits.put(Material.DEEPSLATE,2);
    }

    @Override
    public void onDisable() {
    }
}
