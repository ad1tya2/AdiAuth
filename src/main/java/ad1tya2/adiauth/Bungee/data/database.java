package ad1tya2.adiauth.Bungee.data;

import ad1tya2.adiauth.Bungee.AdiAuth;
import ad1tya2.adiauth.Bungee.Config;
import ad1tya2.adiauth.Bungee.utils.tools;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class database {
    private static HikariDataSource ds;
    private static final String h2Url = "https://repo1.maven.org/maven2/com/h2database/h2/1.4.200/h2-1.4.200.jar";
    private static final String h2DriverName = "h2-1.4.200.jar";
    public static void load(){
        try {
            HikariConfig config = new HikariConfig();
            if(Config.storageType == Config.StorageType.Mysql) {
                config.setUsername(Config.MYSQL.username.value);
                config.setPassword(Config.MYSQL.password.value);
                config.setJdbcUrl("jdbc:mysql://" + Config.MYSQL.host.value + "/" + Config.MYSQL.database.value+"?rewriteBatchedStatements=true");
                Properties optimizedProps = new Properties();
                optimizedProps.load(AdiAuth.instance.getResourceAsStream("mysql.props"));
                config.setDataSourceProperties(optimizedProps);
            } else {
                tools.loadLibrary(h2Url, h2DriverName);
//                config.setDriverClassName("org.h2.jdbcx.JdbcDataSource");
                config.setDriverClassName("org.h2.Driver");
                config.setJdbcUrl("jdbc:h2:./"+AdiAuth.instance.getDataFolder()+"/users;MODE=MYSQL");
            }
            config.setPoolName("AdiAuthDatabase");
            ds = new HikariDataSource(config);
        } catch (Exception e){
            e.printStackTrace();
            tools.log("&cStorage failed to initialize!, Shutting down server.");
            AdiAuth.instance.getProxy().stop();
        }
    }


    public static void close(){
        try {
            ds.close();
        } catch (Exception ignored){}
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

}
