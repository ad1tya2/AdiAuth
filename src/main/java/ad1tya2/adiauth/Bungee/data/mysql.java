package ad1tya2.adiauth.Bungee.data;

import ad1tya2.adiauth.Bungee.AdiAuth;
import ad1tya2.adiauth.Bungee.Config;
import ad1tya2.adiauth.Bungee.utils.tools;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class mysql {
    private static HikariDataSource ds;
    public static void load(){
        try {
            HikariConfig config = new HikariConfig();
            config.setUsername(Config.MYSQL.username.value);
            config.setPassword(Config.MYSQL.password.value);
            config.setJdbcUrl("jdbc:mysql://" + Config.MYSQL.host.value + "/" + Config.MYSQL.database.value);
            Properties optimizedProps = new Properties();
            optimizedProps.load(AdiAuth.instance.getResourceAsStream("mysql.props"));
            config.setDataSourceProperties(optimizedProps);
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
