package me.dave.ajparkourextras.config;

import me.dave.ajparkourextras.AJParkourExtras;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private int leaderboardRange;
    private boolean purgeOutdated;
    private String noDataFormat;

    public ConfigManager() {
        AJParkourExtras.getInstance().saveDefaultConfig();
    }

    public void reloadConfig() {
        FileConfiguration config = AJParkourExtras.getInstance().getConfig();

        leaderboardRange = config.getInt("leaderboard-range", 30);
        purgeOutdated = config.getBoolean("purge-outdated", false);
        noDataFormat = config.getString("no-data-format", "");
    }

    public int getLeaderboardRange() {
        return leaderboardRange;
    }

    public boolean shouldPurgeOutdated() {
        return purgeOutdated;
    }

    public String getNoDataFormat() {
        return noDataFormat;
    }
}
