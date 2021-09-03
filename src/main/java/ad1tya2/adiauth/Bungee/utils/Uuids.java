package ad1tya2.adiauth.Bungee.utils;

import ad1tya2.adiauth.Bungee.Config;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class Uuids {
    public static UUID getUUidFromUndashedString(String str){
        return java.util.UUID.fromString(
                str
                        .replaceFirst(
                                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
                        )
        );
    }

    public static Optional<UUID> getMojangUUid(String name){
        try {
            String url = "https://api.mojang.com/users/profiles/minecraft/" + name;
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet req = new HttpGet(url);
            CloseableHttpResponse response = client.execute(req);
            switch (response.getCode()){
                case 200:
                    JsonParser parser = new JsonParser();
                    return Optional.of(getUUidFromUndashedString(parser.parse(
                            tools.getString(response.getEntity().getContent())).getAsJsonObject().get("id").getAsString()));
                case 204:
                    return Optional.empty();
                default:
                    return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    public static Optional<UUID> getBackupServerUUID(String name) {
        try {
            String url = Config.backupServer + name;
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet req = new HttpGet(url);
            CloseableHttpResponse response = client.execute(req);
            switch (response.getCode()){
                case 200:
                    return Optional.of(getUUidFromUndashedString(tools.getString(response.getEntity().getContent())));
                case 204:
                    return Optional.empty();
                default:
                    return null;
            }
        } catch (IOException e) {
            return null;
        }
    }


    public static String getUndashedUuid(UUID id){
        return id.toString().replaceAll("-", "");
    }
}
