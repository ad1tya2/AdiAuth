package ad1tya2.adiauth.Bungee.data;

import ad1tya2.adiauth.Bungee.AdiAuth;
import ad1tya2.adiauth.Bungee.Config;
import ad1tya2.adiauth.Bungee.utils.URLClassLoaderAccess;
import ad1tya2.adiauth.Bungee.utils.tools;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class database {
    private static HikariDataSource ds;
    private static final String h2Url = "https://repo1.maven.org/maven2/com/h2database/h2/1.4.200/h2-1.4.200.jar";
    private static final String h2DriverName = "plugins/AdiAuth/libs/h2-1.4.200.jar";
    public static void load(){
        try {
            HikariConfig config = new HikariConfig();
            if(Config.storageType == Config.StorageType.Mysql) {
                config.setUsername(Config.MYSQL.username.value);
                config.setPassword(Config.MYSQL.password.value);
                config.setJdbcUrl("jdbc:mysql://" + Config.MYSQL.host.value + "/" + Config.MYSQL.database.value);
                Properties optimizedProps = new Properties();
                optimizedProps.load(AdiAuth.instance.getResourceAsStream("mysql.props"));
                config.setDataSourceProperties(optimizedProps);
            } else {
                File library = new File(h2DriverName);
                if(!library.exists()){
                    tools.log("&eDownloading h2 driver..");
                    downloadLibrary(h2Url, h2DriverName);
                    tools.log("&2Downloaded!");
                }
                loadLibrary(library);
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


    public static void downloadLibrary(String link, String fileName) throws IOException {
            URL url = new URL(link);
            InputStream in = url.openStream();
            Files.copy(in, Paths.get(fileName));
    }

    public static void loadLibrary(File jar) throws MalformedURLException {
        URLClassLoaderAccess access = URLClassLoaderAccess.create((URLClassLoader) AdiAuth.instance.getClass().getClassLoader());
        access.addURL(jar.toURI().toURL());
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
