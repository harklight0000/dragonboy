package database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import config.Config;
import logger.MyLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlFetcher {

    public static String DB_NAME = Config.gI().getDB_NAME();
    public static boolean LOG_QUERY = Config.gI().isDB_LOG_QUERY();
    private static String DRIVER = Config.gI().getDB_DRIVER();
    private static String URL;
    private static String DB_HOST = Config.gI().getDB_HOST();
    private static String DB_PORT = String.valueOf(Config.gI().getDB_PORT());
    private static String DB_USER  = Config.gI().getDB_USER();
    private static String DB_PASSWORD = Config.gI().getDB_PASS();
    private static int MIN_CONN = Config.gI().getDB_MINCONN();
    private static int MAX_CONN = Config.gI().getDB_MAXCONN();
    private static long MAX_LIFE_TIME = Config.gI().getDB_MAX_LIFETIME();
    private static HikariConfig config;
    private static final HikariDataSource ds;


    static {
        HikariDataSource tmp = null;
        try {
            HikariConfig cfg = createConfig("PoolNgocRong", DB_NAME);
            cfg.setInitializationFailTimeout(0);
            tmp = new HikariDataSource(cfg);
            try (Connection c = tmp.getConnection()) {}
            MyLogger.logInformation("connected to " + DB_HOST + " " + DB_NAME);
        } catch (Throwable t) {
            MyLogger.logError(t, "Fail to connect database");
            throw new ExceptionInInitializerError(t);
        } finally {
            ds = tmp;
        }
    }


    public static Connection getConnection() throws SQLException {
        return SqlFetcher.ds.getConnection();
    }

    public static void close() {
        SqlFetcher.ds.close();
    }

//    public static IResultSaeQuery(final String query) throws Exception {
//        try {
//            Connection con = getConnection();
//            try (PreparedStatement ps = con.prepareStatement(query)) {
//                try (ResultSet rs = ps.executeQuery()) {
//                    if (NDatabase.LOG_QUERY) {
//                        Logger.warn( "Thực thi thành công câu lệnh: " + ps.toString() );
//                    }
//                    return new ResultSetImpl(rs);
//                }
//            } finally {
//                if (con != null) {
//                    con.close();
//                }
//            }
//        } catch (Exception ex) {
//            Logger.warn("Có lỗi xảy ra khi thực thi câu lệnh: " + query );
//            throw ex;
//        }
//    }

    public static MyResultSet executeQuery(final String query, final Object... objs) throws Exception {
        try (final Connection con = getConnection(); final PreparedStatement ps = con.prepareStatement(query)) {
            for (int i = 0; i < objs.length; ++i) {
                ps.setObject(i + 1, objs[i]);
            }
            if (SqlFetcher.LOG_QUERY) {
                MyLogger.logWarning("Query executed successfully: " + ps.toString() );
            }
            return new MyResultSetImpl(ps.executeQuery());
        } catch (final Exception ex) {
            MyLogger.logWarning( "An error occurred while executing the query: " + query );
            throw ex;
        }
    }

    public static int executeUpdate(final String query) throws Exception {
        int rowUpdated = -1;
        try (final Connection con = getConnection(); final PreparedStatement ps = con.prepareStatement(query)) {
            if (SqlFetcher.LOG_QUERY) {
                MyLogger.logWarning( "Query executed successfully: " + ps.toString());
            }
            rowUpdated = ps.executeUpdate();
        } catch (final Exception e) {
            MyLogger.logWarning( "An error occurred while executing the query: " + query);
            throw e;
        }
        return rowUpdated;
    }

    public static int executeUpdate(String query, final Object... objs) throws Exception {
        if (query.indexOf("insert") == 0 && query.lastIndexOf("()") == query.length() - 2) {
            final StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i = 0; i < objs.length; ++i) {
                sb.append("?");
                if (i < objs.length - 1) {
                    sb.append(",");
                } else {
                    sb.append(")");
                }
            }
            query = query.replace("()", sb.toString());
        }
        try (final Connection con = getConnection(); final PreparedStatement ps = con.prepareStatement(query)) {
            for (int j = 0; j < objs.length; ++j) {
                ps.setObject(j + 1, objs[j]);
            }
            if (SqlFetcher.LOG_QUERY) {
                MyLogger.logWarning( "Query executed successfully: " + ps.toString() );
            }
            return ps.executeUpdate();
        } catch (final Exception ex) {
            MyLogger.logWarning("An error occurred while executing the query: " + query);
            throw ex;
        }
    }

    private static HikariConfig createConfig(String poolName, String databaseName) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(DRIVER);
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?useUnicode=yes&characterEncoding=UTF-8", DB_HOST, DB_PORT, databaseName));
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);
        config.setMinimumIdle(MIN_CONN);
        config.setMaximumPoolSize(MAX_CONN);
        config.setMaxLifetime(MAX_LIFE_TIME);
        config.setPoolName(poolName);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        return config;
    }
}