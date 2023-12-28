package me.dave.ajparkourextras.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.dave.ajparkourextras.AJParkourExtras;
import me.dave.ajparkourextras.manager.TopManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.ajg0702.parkour.top.TopEntry;

public class Placeholders extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "ajpe";
    }

    @Override
    public @NotNull String getAuthor() {
        return AJParkourExtras.getInstance().getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return AJParkourExtras.getInstance().getDescription().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        params = params.replaceAll("_nocache", "");

        if (params.matches("stats_top_name_[1-9][0-9]*$")) {
            int number = Integer.parseInt(params.split("stats_top_name_")[1]);
            TopEntry pos = TopManager.getInstance().getTop(number, null);

            if(pos.getName().equalsIgnoreCase("--")) {
                return AJParkourExtras.getInstance().getConfigManager().getNoDataFormat();
            }

            return pos.getName();
        }
        if (params.matches("stats_top_name_[1-9][0-9]*_.+$")) {
            int number = Integer.parseInt(params.split("_")[3]);
            String area = params.split("_")[4];
            TopEntry pos = TopManager.getInstance().getTop(number, null /*TODO: replace with "area" variable*/);

            if(pos.getName().equalsIgnoreCase("--")) {
                return AJParkourExtras.getInstance().getConfigManager().getNoDataFormat();
            }

            return pos.getName();
        }

        if (params.matches("stats_top_score_[1-9][0-9]*$")) {
            int number = Integer.parseInt(params.split("stats_top_score_")[1]);
            TopEntry pos = TopManager.getInstance().getTop(number, null);

            if(pos.getName().equalsIgnoreCase("--")) {
                return AJParkourExtras.getInstance().getConfigManager().getNoDataFormat();
            }

            return String.valueOf(pos.getScore());
        }
        if (params.matches("stats_top_score_[1-9][0-9]*_.+$")) {
            int number = Integer.parseInt(params.split("_")[3]);
            String area = params.split("_")[4];
            TopEntry pos = TopManager.getInstance().getTop(number, null /*TODO: replace with "area" variable*/);

            if(pos.getName().equalsIgnoreCase("--")) {
                return AJParkourExtras.getInstance().getConfigManager().getNoDataFormat();
            }

            return String.valueOf(pos.getScore());
        }


        return null;
    }
}
