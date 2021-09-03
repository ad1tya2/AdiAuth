package ad1tya2.adiauth.Bungee.utils;

import ad1tya2.adiauth.Bungee.UserProfile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class passwordUtils {
    public static boolean comparePass(UserProfile profile, String pass){
        if(profile.password.startsWith("JPREMIUM")){
            String[] rawPass = profile.password.substring(8).split("\\$");
            String salt = rawPass[0];
            String saltedPass = rawPass[1];
            String sha256NewPass = getSha256(pass);
            if(getSha256(sha256NewPass+salt).equals(saltedPass)){
                profile.password = sha256NewPass;
                return true;
            } else {
                return false;
            }
        } else {
            return getSha256(pass).equals(profile.password);
        }
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
