package me.dave.ajparkourextras;

import me.dave.ajparkourextras.config.ConfigManager;
import me.dave.ajparkourextras.data.Games;
import me.dave.ajparkourextras.hook.Placeholders;
import me.dave.ajparkourextras.listener.ParkourListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AJParkourExtras extends JavaPlugin {
    private static AJParkourExtras plugin;
    private ConfigManager configManager;
    private Games games;
    private Placeholders placeholders;

    @Override
    public void onEnable() {
        plugin = this;

        configManager = new ConfigManager();
        configManager.reloadConfig();

        games = new Games();

        placeholders = new Placeholders();
        placeholders.register();

        Bukkit.getPluginManager().registerEvents(new ParkourListener(), this);
    }

    @Override
    public void onDisable() {
        placeholders.unregister();
        games.disable();

        plugin = null;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public Games getGames() {
        return games;
    }

    public static AJParkourExtras getInstance() {
        return plugin;
    }
}
