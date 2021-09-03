package ad1tya2.adiauth.Bungee.data;

import ad1tya2.adiauth.Bungee.AdiAuth;
import ad1tya2.adiauth.Bungee.Config;
import ad1tya2.adiauth.Bungee.UserProfile;
import ad1tya2.adiauth.Bungee.utils.Uuids;
import ad1tya2.adiauth.Bungee.utils.tools;
import com.google.common.base.Charsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static ad1tya2.adiauth.Bungee.utils.Uuids.getBackupServerUUID;
import static ad1tya2.adiauth.Bungee.utils.Uuids.getUndashedUuid;

public class storage {
    public enum AccountType {
        TOTAL, CRACKED, PREMIUM
    }
    private static final ConcurrentHashMap<String, UserProfile> pMap = new ConcurrentHashMap<String, UserProfile>();
    private static final ConcurrentHashMap<UUID, UserProfile> pMapByPremiumUuid = new ConcurrentHashMap<UUID, UserProfile>();
    private static final ConcurrentHashMap<String, List<UserProfile>> profilesByIp = new ConcurrentHashMap<String, List<UserProfile>>();
    private static final ConcurrentHashMap<String, UserProfile> pMapByDiscord = new ConcurrentHashMap<String, UserProfile>();
    public static Integer getAccounts(String ip, AccountType type){
        List<UserProfile> profiles = profilesByIp.get(ip);
        if(profiles == null){
            return 0;
        }
        switch (type){
            case TOTAL:
                return profiles.size();
            case CRACKED:
                int accounts = 0;
                for(UserProfile profile: profiles){
                    if(!profile.isPremium()){
                        accounts++;
                    }
                }
                return accounts;
            case PREMIUM:
                int premiums = 0;
                for(UserProfile profile: profiles){
                    if(profile.isPremium()){
                        premiums++;
                    }
                }
                return premiums;
            default:
                return 0;
        }
    }
    public static void addAccountToIpList(UserProfile profile){
        if(profile.lastIp == null || !profile.fullJoined){
            return;
        }
        List<UserProfile> profiles = profilesByIp.get(profile.lastIp);
        if(profiles == null){
            profiles = new ArrayList<UserProfile>();
        }
        if(!profiles.contains(profile)) {
            profiles.add(profile);
        }
        profilesByIp.put(profile.lastIp, profiles);
    }
    public static void load(){
        try {
            tools.log("&eLoading Players...");
            Connection conn = database.getConnection();
            Statement stmt = conn.createStatement();
            int users = 0;
            stmt.execute("CREATE TABLE IF NOT EXISTS auth_users( uuid CHAR(36) PRIMARY KEY, lastIp VARCHAR(40), password VARCHAR(256), username VARCHAR(16), " +
                    "premiumUuid CHAR(36), fullJoined BOOLEAN, discordId VARCHAR(20) )");

            //Adding column if not exists
            //I did this for the new discord feature that was added
            DatabaseMetaData md = conn.getMetaData();
            if(!(md.getColumns(null, null, "AUTH_USERS", "DISCORDID").next() ||
                    md.getColumns(null, null, "auth_users", "discordId").next())){
                //Column dosent exist,
                stmt.execute("ALTER TABLE auth_users ADD discordId VARCHAR(20);");
            }
            ResultSet records = stmt.executeQuery("SELECT uuid, lastIp, password, username, premiumUuid, fullJoined, discordId FROM auth_users");
            while (records.next()){
                users++;
                UserProfile user = new UserProfile();
                user.uuid = UUID.fromString(records.getString(1));
                user.lastIp = records.getString(2);
                user.password = records.getString(3);
                user.username = records.getString(4);
                try {
                    user.premiumUuid = UUID.fromString(records.getString(5));
                } catch (Exception e){
                    user.premiumUuid = null;
                }
                user.fullJoined = records.getBoolean(6);
                user.discordId = records.getString(7);
                updatePlayerMemory(user);
            }
            records.close();
            stmt.close();
            tools.log("&bLoad complete!, loaded &2"+users+" &bUsers");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void logApiError(){
        tools.log(Level.SEVERE, "&cMojang api and/or Backup server is not responding, Please check, &eWill login the player using the last updated data");
    }

    public static Optional<UserProfile> getPlayerForLogin(String name, String ip){
                UserProfile user = new UserProfile();
                user.username = name;
                user.lastIp = ip;
                UserProfile oldUserByName = pMap.get(name);
                if(oldUserByName == null && getAccounts(ip, AccountType.TOTAL)>=Config.maxTotalAccounts){
                  return null;
                }
                Optional<UUID> uuid;
                if(Config.forceBackupServer){
                    uuid = Uuids.getBackupServerUUID(name);
                }
                else {
                    uuid = Uuids.getMojangUUid(name);
                    if (uuid == null) {
                        if (Config.backupServerEnabled) {
                            uuid = getBackupServerUUID(name);
                            tools.log(Level.WARNING, "&eUsing backup server for " + name);
                        }
                    }
                }
                if(uuid == null){
                    logApiError();
                    if(oldUserByName == null){
                        return Optional.empty();
                    }else {
                        return Optional.of(oldUserByName);
                    }
                }

                    //Premium
                    if(uuid.isPresent()) {
                        if(oldUserByName == null && getAccounts(ip, AccountType.PREMIUM)>=Config.maxPremiumAccounts){
                            return null;
                        }
                        user.premiumUuid = uuid.get();
                        user.uuid = user.premiumUuid;
                        UserProfile oldPremiumUser = pMapByPremiumUuid.get(user.premiumUuid);
                        if (oldPremiumUser == null) {
                            if (oldUserByName != null && !(oldUserByName.isPremium())) {
                                oldUserByName.lastIp = ip;
                                if (Config.convertOldCrackedToPremium) {
                                    oldUserByName.premiumUuid = user.premiumUuid;
                                }
                                updatePlayerMemory(oldUserByName);
                                return Optional.of(oldUserByName);
                            }
                            updatePlayerMemory(user);
                            return Optional.of(user);
                        } else if (oldPremiumUser.username != user.username) {
                            //Username change event
                            oldPremiumUser.username = user.username;
                            oldPremiumUser.lastIp = ip;
                            updatePlayerMemory(oldPremiumUser);
                            return Optional.of(oldPremiumUser);
                        } else {
                            if (oldPremiumUser.lastIp != ip) {
                                oldPremiumUser.lastIp = ip;
                                updatePlayerMemory(oldPremiumUser);
                            }
                            return Optional.of(oldPremiumUser);
                        }
                    }

                    //Cracked
                    else {
                        if(oldUserByName == null && getAccounts(ip, AccountType.CRACKED)>=Config.maxCrackedAccounts){
                            return null;
                        }
                        user.uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
                        user.premiumUuid = null;

                        if (oldUserByName == null) {
                            updatePlayerMemory(user);

                            return Optional.of(user);
                        }

                        //If the username of this player has been converted to cracked from premium
                        //i.e If someone had a premium account with this name in the past and has changed his id to something else
                        try {
                            if (oldUserByName.isPremium() && oldUserByName.uuid != user.uuid) {
                                CloseableHttpClient client = HttpClients.createDefault();
                                HttpGet reqx = new HttpGet("https://api.mojang.com/user/profiles/" + getUndashedUuid(oldUserByName.premiumUuid) + "/names");
                                CloseableHttpResponse response = client.execute(reqx);
                                JsonParser parser = new JsonParser();
                                JsonArray rawUsernames = parser.parse(tools.getString(response.getEntity().getContent())).getAsJsonArray();
                                oldUserByName.username = rawUsernames.get(rawUsernames.size() - 1).getAsJsonObject().get("name").getAsString();
                                updatePlayer(oldUserByName);
                                updatePlayerMemory(user);
                                response.close();
                                client.close();
                            }
                        }catch (Exception ignored){}


                        if (!oldUserByName.lastIp.equals(user.lastIp)) {
                            user.endSession();
                        }
                        return Optional.of(user);
                    }
    }

    public static UserProfile getPlayerMemory(String name){
        return pMap.get(name);
    }


    public static void asyncUserProfileUpdate(UserProfile profile){
        AdiAuth.instance.getProxy().getScheduler().runAsync(AdiAuth.instance, new Runnable() {
            @Override
            public void run() {
                try {
                    Connection conn = database.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                            "REPLACE INTO auth_users(uuid, lastIp, password, username, premiumUuid, fullJoined, discordId) VALUES(?, ?, ?, ?, ?, ?, ?) ");
                    stmt.setString(1, profile.uuid.toString());
                    stmt.setString(2, profile.lastIp);
                    stmt.setString(3, profile.password);
                    stmt.setString(4, profile.username);
                    //If uuid is null then put null, if not then put string value of uuid
                    stmt.setString(5, profile.premiumUuid == null? null: profile.premiumUuid.toString());
                    stmt.setBoolean(6, profile.fullJoined);
                    stmt.setString(7, profile.discordId);
                    stmt.executeUpdate();
                    stmt.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void updatePlayer(UserProfile player){
        updatePlayerMemory(player);
        asyncUserProfileUpdate(player);
    }

    public static UserProfile getPlayerByDiscord(String discordId){
        return pMapByDiscord.get(discordId);
    }

    private static void updatePlayerMemory(UserProfile profile){
        pMap.put(profile.username, profile);
        if(profile.premiumUuid != null) {
            pMapByPremiumUuid.put(profile.premiumUuid, profile);
        }
        if(profile.discordId != null){
            pMapByDiscord.put(profile.discordId, profile);
        }
        addAccountToIpList(profile);
    }


}
