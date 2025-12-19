package data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

public class DBConnection {
    // Connection pools for Auth DB and ERP DB
    private static HikariDataSource authSource;
    private static HikariDataSource erpSource;

    static {
        try {
            Properties props = new Properties();
            InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties");
            System.out.println("Resource = " + DBConnection.class.getClassLoader().getResource("db.properties"));
            props.load(in);

            // Auth DB
            HikariConfig authCfg = new HikariConfig();
            authCfg.setJdbcUrl(props.getProperty("auth.url"));
            authCfg.setUsername(props.getProperty("auth.user"));
            authCfg.setPassword(props.getProperty("auth.password"));
            authCfg.setMaximumPoolSize(Integer.parseInt(props.getProperty("maximumPoolSize", "10")));
            authSource = new HikariDataSource(authCfg);

            // ERP DB
            HikariConfig erpCfg = new HikariConfig();
            erpCfg.setJdbcUrl(props.getProperty("erp.url"));
            erpCfg.setUsername(props.getProperty("erp.user"));
            erpCfg.setPassword(props.getProperty("erp.password"));
            erpCfg.setMaximumPoolSize(Integer.parseInt(props.getProperty("maximumPoolSize", "10")));
            erpSource = new HikariDataSource(erpCfg);

            System.out.println("Database pools initialized");
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get connection for Auth database
    public static Connection getAuthConnection() throws Exception {
        return authSource.getConnection();
    }

    // Get connection for ERP database
    public static Connection getErpConnection() throws Exception {
        return erpSource.getConnection();
    }

    // Close both connection pools
    public static void close() {
        if (authSource != null) authSource.close();
        if (erpSource != null) erpSource.close();
    }
}