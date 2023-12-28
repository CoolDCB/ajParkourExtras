package me.dave.ajparkourextras.listener;

import me.dave.ajparkourextras.AJParkourExtras;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.ajg0702.parkour.api.events.PlayerEndParkourEvent;

import java.util.UUID;

public class ParkourListener implements Listener {

    @EventHandler
    public void onPlayerEndParkour(PlayerEndParkourEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        int score = event.getFallScore();

        AJParkourExtras.getInstance().getGames().addGame(uuid, score, -1, null);
    }
}
