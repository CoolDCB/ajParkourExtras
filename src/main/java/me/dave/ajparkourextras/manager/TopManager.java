package me.dave.ajparkourextras.manager;

import me.dave.ajparkourextras.AJParkourExtras;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.ajg0702.parkour.top.TopEntry;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class TopManager {
    private static TopManager instance;
    private final HashMap<String, HashMap<Integer, Long>> lastGet = new HashMap<>();
    private final HashMap<String, HashMap<Integer, TopEntry>> cache = new HashMap<>();
    private final ConcurrentHashMap<Player, HashMap<String, Integer>> highScores = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Player, HashMap<String, Long>> lastGetHS = new ConcurrentHashMap<>();
    long lastClean = 0L;

    public static TopManager getInstance() {
        if (instance == null) {
            instance = new TopManager();
        }

        return instance;
    }

    public TopEntry getTop(int position, String area) {
        if (area == null) {
            area = "overall";
        }

        if (!this.cache.containsKey(area)) {
            this.cache.put(area, new HashMap<>());
        }

        if (!this.lastGet.containsKey(area)) {
            this.lastGet.put(area, new HashMap<>());
        }

        if ((this.cache.get(area)).containsKey(position)) {
            if (System.currentTimeMillis() - (this.lastGet.get(area)).get(position) > 5000L) {
                this.lastGet.get(area).put(position, System.currentTimeMillis());
                this.fetchPositionAsync(position, area);
            }

            return (this.cache.get(area)).get(position);
        } else {
            (this.lastGet.get(area)).put(position, System.currentTimeMillis());
            return this.fetchPosition(position, area);
        }
    }

    private void fetchPositionAsync(int position, String area) {
        Bukkit.getScheduler().runTaskAsynchronously(AJParkourExtras.getInstance(), () -> this.fetchPosition(position, area));
    }

    private TopEntry fetchPosition(int position, String area) {
        TopEntry te = AJParkourExtras.getInstance().getGames().getTopPosition(position, area);
        (this.cache.get(area)).put(position, te);
        return te;
    }

    public int getHighScore(Player player, String area) {
        if (area == null) {
            area = "overall";
        }

        if (!this.highScores.containsKey(player)) {
            this.highScores.put(player, new HashMap<>());
        }

        if (!this.lastGetHS.containsKey(player) || this.lastGetHS.get(player) == null) {
            this.lastGetHS.put(player, new HashMap<>());
        }

        if ((this.highScores.get(player)).containsKey(area) && (this.lastGetHS.get(player)).containsKey(area)) {
            if (Calendar.getInstance().getTimeInMillis() - (this.lastGetHS.get(player)).get(area) > 1000L) {
                (this.lastGetHS.get(player)).put(area, System.currentTimeMillis());
                this.fetchHighScoreAsync(player, area);
            }

            return (this.highScores.get(player)).get(area);
        } else {
            (this.lastGetHS.get(player)).put(area, System.currentTimeMillis());
            return this.fetchHighScore(player, area);
        }
    }

    private void fetchHighScoreAsync(Player player, String area) {
        Bukkit.getScheduler().runTaskAsynchronously(AJParkourExtras.getInstance(), () -> this.fetchHighScore(player, area));
        Bukkit.getScheduler().runTaskAsynchronously(AJParkourExtras.getInstance(), () -> {
            if ((double)(System.currentTimeMillis() - this.lastClean) > 300000.0) {
                this.lastClean = System.currentTimeMillis();
                Iterator<Player> var2 = this.highScores.keySet().iterator();

                Player key;
                while(var2.hasNext()) {
                    key = var2.next();
                    if (!key.isOnline()) {
                        this.highScores.remove(player);
                    }
                }

                var2 = this.lastGetHS.keySet().iterator();

                while(var2.hasNext()) {
                    key = var2.next();
                    if (!key.isOnline()) {
                        this.lastGetHS.remove(player);
                    }
                }
            }

        });
    }

    private int fetchHighScore(Player player, String area) {
        int highScore = AJParkourExtras.getInstance().getGames().getHighScore(player.getUniqueId(), area);
        if (!this.highScores.containsKey(player)) {
            this.highScores.put(player, new HashMap<>());
        }

        (this.highScores.get(player)).put(area, highScore);
        return highScore;
    }

    public void clearPlayerCache(Player ply) {
        this.highScores.remove(ply);
    }

    public HashMap<Player, HashMap<String, Integer>> getHighScores() {
        return new HashMap<>(this.highScores);
    }

    public HashMap<Player, HashMap<String, Long>> getLastGetHS() {
        return new HashMap<>(this.lastGetHS);
    }

    public HashMap<String, HashMap<Integer, Long>> getLastGet() {
        return this.lastGet;
    }

    public HashMap<String, HashMap<Integer, TopEntry>> getCache() {
        return this.cache;
    }
}
