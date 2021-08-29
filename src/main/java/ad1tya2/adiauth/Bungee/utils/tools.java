package ad1tya2.adiauth.Bungee.utils;

import ad1tya2.adiauth.Bungee.AdiAuth;
import net.md_5.bungee.api.ChatColor;


import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

    public static String getSha256(String str){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash); // make it printable
        }catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    private static String bytesToHex(byte[] hash) {
        StringBuilder result = new StringBuilder();
        for (byte b : hash) result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }
}
