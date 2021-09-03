package ad1tya2.adiauth.Bungee.commands;

import ad1tya2.adiauth.Bungee.AdiAuth;
import ad1tya2.adiauth.Bungee.data.storage;
import ad1tya2.adiauth.Bungee.utils.Uuids;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.sql.*;

public class converter extends Command {
    public converter(){
        super("adiauthconverter", "adiauth.admin", "aaconverter", "adiconvert");
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            String host=null, database=null, dbUsername=null, dbPassword=null;
            for (String arg : args) {
                try {
                    String[] rawArg = arg.split("=");
                    String key = rawArg[0];
                    String value = rawArg[1];
                    switch (key){
                        case "host":
                            host = value;
                            break;
                        case "database":
                            database = value;
                            break;
                        case "username":
                            dbUsername = value;
                            break;
                        case "password":
                            dbPassword = value;
                            break;
                        default:
                            break;
                    }
                } catch (Exception ignored) {
                }
            }
            if (args[0].equalsIgnoreCase("jpremium")) {
                sender.sendMessage("Trying to connect!");
                Connection conn = DriverManager.getConnection("jdbc:mysql://"+host+"/"+database+"?" +
                                "user="+dbUsername+"&password="+dbPassword);
                Statement stmt = conn.createStatement();
                sender.sendMessage(ChatColor.GREEN+"Connected!" +
                        "\nQuerying Records...");
                ResultSet jpremRecords =
                        stmt.executeQuery("SELECT uniqueId, premiumId, lastNickname, hashedPassword,lastAddress FROM user_profiles");
                Connection currentDb = ad1tya2.adiauth.Bungee.data.database.getConnection();
                PreparedStatement newRecords = currentDb.prepareStatement(
                        "REPLACE INTO auth_users(uuid, lastIp , password, username, premiumUuid, fullJoined) " +
                                "VALUES(?, ?, ?, ?, ?, ?)"
                );
                int count = 0;
                while (jpremRecords.next()){
                    String username = jpremRecords.getString("lastNickname");



                    String uuid = Uuids.getUUidFromUndashedString(jpremRecords.getString("uniqueId")).toString();
                    String premiumId = jpremRecords.getString("premiumId") == null? null:
                            Uuids.getUUidFromUndashedString(jpremRecords.getString("premiumId")).toString();
                    String lastIp = jpremRecords.getString("lastAddress");
                    String password = jpremRecords.getString("hashedPassword");

                    if(username == null || (premiumId == null && password == null) || storage.getPlayerMemory(username) != null){
                        //Skip record if player already exists in database
                        //Or if premium id and password are both null. indicating that the player has never joined the server
                        continue;
                    }
                    if(password != null){
                        password = password.replace("SHA256$", "JPREMIUM");
                    }
                    newRecords.setString(1, uuid);
                    newRecords.setString(2, lastIp);
                    newRecords.setString(3, password);
                    newRecords.setString(4, username);
                    newRecords.setString(5, premiumId);
                    newRecords.setBoolean(6, true);
                    newRecords.addBatch();
                    count++;
                }
                sender.sendMessage(ChatColor.YELLOW+"Starting import..... This may take a while depending on the size of your database!\n" +
                        count+" Records!");
                newRecords.executeBatch();
                sender.sendMessage(ChatColor.BLUE+"Database successfully imported!");
                sender.sendMessage(ChatColor.GREEN+"Reloading all data!");
                AdiAuth.reload();
                sender.sendMessage(ChatColor.GREEN+"Successfully reloaded!");
                jpremRecords.close();
                stmt.close();
                conn.close();
                newRecords.close();
            } else {
                sender.sendMessage(ChatColor.RED+"Invalid converter!");
            }
        } catch (Exception e){
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED+"Error, please look in the console for info!");
        }
    }
}
