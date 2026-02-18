package config;

import lombok.Getter;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

@Getter
public final class Config {
    private static final String CONFIG_PATH = "Config.properties";
    private static Config instance;

    // Instance variables are now `final` and set by the constructor
    private final boolean SV_LOCAL;
    private final boolean SV_TEST;
    private final boolean SV_DAO_AUTO_UPDATE;
    private final String SV_NAME;
    private final String SV_IP;
    private final String SV_DOMAIN;
    private final int SV_PORT;
    private final int SV_WAIT_LOGIN;
    private final int SV_MAXPERIP;
    private final int SV_MAXPLAYER;
    private final int SV_RATE_EXP_SERVER;

    private final String DB_DRIVER;
    private final String DB_HOST;
    private final int DB_PORT;
    private final String DB_NAME;
    private final String DB_USER;
    private final String DB_PASS;
    private final int DB_MINCONN;
    private final int DB_MAXCONN;
    private final int DB_MAX_LIFETIME;
    private final boolean DB_LOG_QUERY;

    // The private constructor now takes a Properties object
    private Config(Properties p) {
        this.SV_LOCAL = bool(p, "server.local", false);
        this.SV_TEST = bool(p, "server.test", false);
        this.SV_DAO_AUTO_UPDATE = bool(p, "server.daoautoupdater", false);
        this.SV_NAME = str(p, "server.name", "ngocrongonline");
        this.SV_IP = str(p, "server.ip", "0.0.0.0");
        this.SV_DOMAIN = str(p, "server.domain", "");
        this.SV_PORT = num(p, "server.port", 14446);
        this.SV_WAIT_LOGIN = num(p, "server.waitlogin", 0);
        this.SV_MAXPERIP = num(p, "server.maxperip", 3);
        this.SV_MAXPLAYER = num(p, "server.maxplayer", 1000);
        this.SV_RATE_EXP_SERVER = num(p, "server.rate_exp", 1);
        
        this.DB_DRIVER = str(p, "database.driver", "com.mysql.jdbc.Driver");
        this.DB_HOST = str(p, "database.host", "localhost");
        this.DB_PORT = num(p, "database.port", 3306);
        this.DB_NAME = str(p, "database.name", this.SV_NAME);
        this.DB_USER = str(p, "database.user", "root");
        this.DB_PASS = str(p, "database.pass", "");
        this.DB_MINCONN = num(p, "database.min", 1);
        this.DB_MAXCONN = num(p, "database.max", 1);
        this.DB_MAX_LIFETIME = num(p, "database.lifetime", 120_000);
        this.DB_LOG_QUERY = bool(p, "database.log", false);
    }

    public static Config gI() {
        if (instance == null) {
            instance = load(CONFIG_PATH);
        }
        return instance;
    }

    public static synchronized void reload() {
        instance = load(CONFIG_PATH);
    }

    private static Config load(String path) {
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)) {
            p.load(r);
        } catch (IOException e) {
            throw new RuntimeException("Could not load " + path, e);
        }
        return new Config(p); // Create and return a new instance
    }
    
    // The helper methods can remain static as they don't depend on an object's state
    private static String str(Properties p, String k, String def) {
        String v = p.getProperty(k);
        return (v == null || v.isEmpty()) ? def : v.trim();
    }

    private static int num(Properties p, String k, int def) {
        try {
            return Integer.parseInt(p.getProperty(k));
        } catch (Exception e) {
            return def;
        }
    }

    private static boolean bool(Properties p, String k, boolean def) {
        String v = p.getProperty(k);
        return (v == null) ? def : Boolean.parseBoolean(v.trim());
    }
}