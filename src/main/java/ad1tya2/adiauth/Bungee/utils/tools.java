package ad1tya2.adiauth.Bungee.utils;

import ad1tya2.adiauth.Bungee.AdiAuth;
import net.md_5.bungee.api.ChatColor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Level;

public class tools {
    public static String getColoured(String msg){
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static void log(String msg){
        AdiAuth.instance.getLogger().log(Level.INFO, getColoured(msg));
    }

    public static void log(Level level, String msg){
        AdiAuth.instance.getLogger().log(level, getColoured(msg));
    }

    public static String getString(InputStream io){
        Scanner s = new Scanner(io).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static String getIp(SocketAddress address){
        return ((InetSocketAddress)address).getAddress().toString().split("/")[1];
    }



    public static void loadLibrary(String link, String fileName) throws IOException {
            fileName = "plugins/AdiAuth/libs/"+fileName;
            if(!fileName.endsWith(".jar")){
                fileName = fileName+".jar";
            }
            File library = new File(fileName);
            if(!library.exists()) {
                log("&2Downloading &b"+fileName+" &2From "+link+"....");
                URL url = new URL(link);
                InputStream in = url.openStream();
                Files.copy(in, Paths.get(fileName));
                log("&2Download complete!");
            }
            URLClassLoaderAccess access = URLClassLoaderAccess.create((URLClassLoader) AdiAuth.instance.getClass().getClassLoader());
             access.addURL(library.toURI().toURL());
    }
}
