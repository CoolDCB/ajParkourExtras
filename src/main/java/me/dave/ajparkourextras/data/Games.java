package me.dave.ajparkourextras.data;

import me.dave.ajparkourextras.AJParkourExtras;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import us.ajg0702.parkour.game.Manager;
import us.ajg0702.parkour.hikari.HikariConfig;
import us.ajg0702.parkour.hikari.HikariDataSource;
import us.ajg0702.parkour.top.TopEntry;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Games {

    File storageConfigFile;
    YamlConfiguration storageConfig;

    String tablePrefix;
    String extrasTablePrefix;

    String method;

    private final HikariConfig hikariConfig = new HikariConfig();
    HikariDataSource ds;

    public Games() {
        AJParkourExtras plugin = AJParkourExtras.getInstance();

        storageConfigFile = new File(plugin.getDataFolder(), "storage.yml");
        storageConfig = YamlConfiguration.loadConfiguration(storageConfigFile);

        checkStorageConfig();

        String ip = storageConfig.getString("mysql.ip");
        String username = storageConfig.getString("mysql.username");
        String password = storageConfig.getString("mysql.password");
        String database = storageConfig.getString("mysql.database");
        String tablePrefix = storageConfig.getString("mysql.tablePrefix");
        String tx_isolation = storageConfig.getString("mysql.tx_isolation");
        boolean useSSL = storageConfig.getBoolean("mysql.useSSL");
        boolean allowPublicKeyRetrieval = storageConfig.getBoolean("mysql.allowPublicKeyRetrieval");
        int minCount = storageConfig.getInt("mysql.minConnections");
        int maxCount = storageConfig.getInt("mysql.maxConnections");

        String sMethod = storageConfig.getString("method");


        if (sMethod.equalsIgnoreCase("mysql")) {
            try {
                initDatabase("mysql", ip, username, password, database, tablePrefix, useSSL, allowPublicKeyRetrieval, minCount, maxCount, tx_isolation);
            } catch (Exception e) {
                plugin.getLogger().warning("Could not connect to database! Switching to sqlite storage. Error: ");
                e.printStackTrace();
                sMethod = "sqlite";
            }
        }
        if (sMethod.equalsIgnoreCase("sqlite") || sMethod.equalsIgnoreCase("yaml")) {
            try {
                initDatabase(sMethod, null, null, null, null, tablePrefix, false, false, minCount, maxCount, tx_isolation);
            } catch(SQLException e) {
                plugin.getLogger().severe("Unable to create sqlite database. High scores will not work!");
                e.printStackTrace();
            }
        }

        if (method == null) {
            plugin.getLogger().severe("Unable to find database method! Check storage.yml as you most likely put in an invalid storage method.");
        }

    }

    private void checkStorageConfig() {
        Map<String, Object> v = new HashMap<>();
        v.put("method", "sqlite");
        v.put("mysql.ip", "127.0.0.1:3306");
        v.put("mysql.username", "");
        v.put("mysql.password", "");
        v.put("mysql.database", "");
        v.put("mysql.tablePrefix", "ajparkour_extras_");
        v.put("mysql.allowPublicKeyRetrieval", false);
        v.put("mysql.useSSL", false);
        v.put("mysql.minConnections", 2);
        v.put("mysql.maxConnections", 10);
        v.put("mysql.tx_isolation", "");

        boolean save = false;

        storageConfig.options().header("\n\nThis file tells the plugin where it\n"
            + "should store player high scores.\n\n"
            + "The method option can either be 'sqlite' or 'mysql'.\n"
            + "If it is mysql, you must configure the mysql section below.\n\n ");
        for (String key : v.keySet()) {
            if (!storageConfig.isSet(key)) {
                storageConfig.set(key, v.get(key));
                save = true;
            }
        }
        if (save) {
            try {
                storageConfig.save(storageConfigFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public Connection getConnection() {
        try {
            if (method.equals("sqlite") || method.equals("yaml")) {
                if (sqliteConn == null || sqliteConn.isClosed()) {
                    sqliteConn = DriverManager.getConnection(url);
                }
                return sqliteConn;
            }

            if (ds == null) {
                return null;
            }
            return ds.getConnection();
        } catch (SQLException e) {
            AJParkourExtras.getInstance().getLogger().warning("Unable to get sql connection:");
            e.printStackTrace();
            return null;
        }
    }

    Connection sqliteConn;
    String url;
    private void initDatabase(String method, String ip, String username, String password, String database, String tablePrefix, boolean useSSL, boolean allowPublicKeyRetrieval, int minConnections, int maxConnections, String tx_isolation) throws SQLException {
        if (method.equals("mysql")) {
            url = "jdbc:mysql://"+ip+"/"+database+"?useSSL="+useSSL+"&allowPublicKeyRetrieval="+allowPublicKeyRetrieval+"&characterEncoding=utf8";
            hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
            hikariConfig.setJdbcUrl(url);
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.setMaximumPoolSize(maxConnections);
            hikariConfig.setMinimumIdle(minConnections);

            if (!tx_isolation.isEmpty()) {
                hikariConfig.setTransactionIsolation(tx_isolation);
            }

            this.tablePrefix = tablePrefix;
            this.extrasTablePrefix = tablePrefix + "extras_";

            ds = new HikariDataSource(hikariConfig);
            ds.setLeakDetectionThreshold(60 * 1000);
        } else {
            url = "jdbc:sqlite:" + AJParkourExtras.getInstance().getDataFolder().getAbsolutePath()+File.separator + "games.db";
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }

            sqliteConn = DriverManager.getConnection(url);
        }
        this.method = method;

        createTables();

        if (method.equalsIgnoreCase("yaml")) {
            this.method = "sqlite";
        }
    }

    public void disable() {
        if (ds != null) {
            ds.close();
        }
        if (sqliteConn != null) {
            try {
                sqliteConn.close();
            } catch (SQLException ignored) {}
        }
    }

    private void createTables() throws SQLException {
        String autoIncrement = "AUTO_INCREMENT";
        String integer = "INT";
        if (method.equalsIgnoreCase("sqlite") || method.equalsIgnoreCase("yaml")) {
            autoIncrement = "AUTOINCREMENT";
            integer = "INTEGER";
        }

        Connection conn = getConnection();
        conn.createStatement().executeUpdate(
            "create table if not exists " + extrasTablePrefix + "games " +
                "(id " + integer + " PRIMARY KEY " + autoIncrement + ", epochTime LONG, area TINYTEXT, player VARCHAR(36), score INT, time INT)"
        );
        closeConn(conn);
    }


    private void closeConn(Connection conn, ResultSet... resultSets) throws SQLException {
        if (method.equalsIgnoreCase("mysql")) {
            conn.close();
        }

        for (ResultSet rs : resultSets) {
            rs.close();
        }
    }

    public void addGame(UUID uuid, int score, int time, final String area) {
        Runnable runnable = () -> {
            String ar = area;
            if (ar == null || ar.equals("null")) {
                ar = "overall";
            }

            try {
                Connection conn = getConnection();
                conn.createStatement().executeUpdate("insert into " + extrasTablePrefix + "games " +
                    "(epochTime, area, player, score, time) values " +
                    "(" + Instant.now().getEpochSecond() + ", '" + ar + "', '" + uuid + "', " + score + ", " + time + ")");
                closeConn(conn);
            } catch (SQLException e) {
                Bukkit.getLogger().severe("[ajParkour] Unable to add game for a player:");
                e.printStackTrace();
            }

        };

        if (Manager.getInstance().pluginDisabling) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(AJParkourExtras.getInstance(), runnable);
        }
    }

    public String getName(UUID uuid) {
        try {
            Connection conn = getConnection();
            ResultSet r = conn.createStatement().executeQuery("select name from "+ tablePrefix +"players where id='"+uuid.toString()+"'");
            if (!r.next()) {
                return null;
            }
            String re = r.getString("name");
            closeConn(conn, r);
            return re;
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[ajParkour] An error occurred when attempting to get a players name:");
            e.printStackTrace();
            return null;
        }
    }

    public int getHighScore(UUID uuid, String area) {
        if (area == null) {
            area = "overall";
        }

        try {
            Connection conn = getConnection();

            long epochLowerBound = Instant.now().minus(AJParkourExtras.getInstance().getConfigManager().getLeaderboardRange(), ChronoUnit.DAYS).getEpochSecond();
            ResultSet resultSet = conn.createStatement().executeQuery(
                "select score from " + extrasTablePrefix + "games where player='" + uuid.toString() + "' and area='" + area + "' and epochTime>=" + epochLowerBound
            );

            if (!resultSet.next()) {
                closeConn(conn, resultSet);
                return 0;
            }
            int score = resultSet.getInt("score");

            closeConn(conn, resultSet);
            return score;
        } catch(SQLException e) {
            AJParkourExtras.getInstance().getLogger().warning("Unable to get score for " + uuid.toString() + ":");
            e.printStackTrace();
            return -1;
        }
    }


    /**
     * It is recommended to use TopManager#getTop instead of this method.
     * @param position The position to fetch
     * @return The TopEntry for the requested position
     */
    public TopEntry getTopPosition(int position, String area) {
        if (area == null) {
            area = "overall";
        }

        try {
            Connection conn = getConnection();

            long epochLowerBound = Instant.now().minus(AJParkourExtras.getInstance().getConfigManager().getLeaderboardRange(), ChronoUnit.DAYS).getEpochSecond();
            ResultSet resultSet = conn.createStatement().executeQuery(
                "select a.* from " + extrasTablePrefix + "games a inner join (select player, max(score) as max_score from " + extrasTablePrefix + "games where area = '" + area + "' and epochTime >= " + epochLowerBound + " GROUP BY player) b on a.player = b.player and a.score = b.max_score where a.area = '" + area + "' AND a.epochTime >= " + epochLowerBound + " order by a.score desc limit " + (position-1) + "," + position
            );

            if (!resultSet.next()) {
                closeConn(conn, resultSet);
                return new TopEntry(position, "--", -1, -1);
            }

            UUID uuid = UUID.fromString(resultSet.getString("player"));
            int score = resultSet.getInt("score");
            int time = resultSet.getInt("time");
            closeConn(conn, resultSet);

            String name = getName(uuid);
            if(name == null) {
                name = Bukkit.getOfflinePlayer(uuid).getName();
            }
            if(name == null) {
                name = "Unknown";
            }

            return new TopEntry(position, name, score, time);
        } catch(SQLException e) {
            AJParkourExtras.getInstance().getLogger().warning("An error occurred while trying to get a top score:");
            e.printStackTrace();
            return new TopEntry(position, "Error", -1, -1);
        }
    }
}
