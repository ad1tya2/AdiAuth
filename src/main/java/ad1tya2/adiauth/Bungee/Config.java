package ad1tya2.adiauth.Bungee;

import ad1tya2.adiauth.Bungee.utils.tools;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Config {
    public static int SessionTime = 120;
    public enum StorageType {
        Mysql, H2
    }
    public static StorageType storageType;

    public static List<ServerInfo> lobbies = new ArrayList<ServerInfo>();
    public static List<ServerInfo> auths = new ArrayList<ServerInfo>();
    public static List<String> whitelistedCommands;

    public static int serverCheckInterval = 30,
            maxPremiumAccounts, maxCrackedAccounts, maxTotalAccounts, authTime;

    public static String backupServer, secondAttempt, pluginMsgConf;

    public static boolean forceBackupServer, backupServerEnabled, secondAttemptEnabled, disconnectOnWrongPass;
    public static class Messages {
        public static String registerError, loginAndRegisterSuccess, alreadyLoggedIn, loginNotRegistered,
                loginWrongPass, loginError, alreadyRegistered, noServersAvailable, registerMessage,
                loginMessage, logoutMessage, changePassError, genericPremiumError, successfulChangePass,
                tooManyAccounts, loginRegisterBossBar, authTimeExceeded, discordRegisterMessage, discordLoginMessage;
        public static Title loginAndRegisterSuccessTitle, registerTitle, loginTitle;
    }
    public static class Discord {
        public static String bot_token, loginButtonChannel, activityStatus, buttonMessage, buttonText, roleToGive;
        public static boolean enabled, compulsory_for_enabled, roleToGiveEnabled;
        public static List<String> compulsory_for;
    }
    public  enum MYSQL {
        username,
        password,
        database,
        host;
        public String value;
    }


    public static Configuration config;
    private static Configuration defaultConf;


    public static void load() {
        saveDefaultConf();
        try {
            //Loading the default config
            defaultConf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(AdiAuth.instance.getResourceAsStream("bungeeconfig.yml"));
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(AdiAuth.instance.getDataFolder(), "config.yml"), defaultConf);
        Configuration mysqlConf = config.getSection("mysql");
        for (MYSQL x : MYSQL.values()) {
            x.value = mysqlConf.getString(x.name());
        }
        SessionTime = config.getInt("sessionTime");
        for (String auth : config.getStringList("authServers")) {
            auths.add(AdiAuth.instance.getProxy().getServerInfo(auth));
        }
        for (String lobby : config.getStringList("lobbyServers")) {
            lobbies.add(AdiAuth.instance.getProxy().getServerInfo(lobby));
        }

        pluginMsgConf = "blindness=" + config.getBoolean("blindness") + ",freezePlayers=" + config.getBoolean("freezePlayers");

         serverCheckInterval = config.getInt("serverCheckInterval");
        whitelistedCommands = config.getStringList("whitelistedCommands");
        Configuration msgConf = config.getSection("messages");
        Messages.registerError = tools.getColoured(msgConf.getString("registerError"));
        Messages.loginAndRegisterSuccess = tools.getColoured(msgConf.getString("loginAndRegisterSuccess"));
        Messages.alreadyLoggedIn = tools.getColoured(msgConf.getString("alreadyLoggedIn"));
        Messages.loginNotRegistered = tools.getColoured(msgConf.getString("loginNotRegistered"));
        Messages.loginWrongPass = tools.getColoured(msgConf.getString("loginWrongPass"));
        Messages.loginError = tools.getColoured(msgConf.getString("loginError"));
        Messages.alreadyRegistered = tools.getColoured(msgConf.getString("alreadyRegistered"));
        Messages.loginAndRegisterSuccessTitle = createTitle(getMsgString("loginAndRegisterSuccessTitle"));
        Messages.noServersAvailable = tools.getColoured(msgConf.getString("noServersAvailable"));
        Messages.registerMessage = tools.getColoured(msgConf.getString("registerMessage"));
        Messages.loginMessage = tools.getColoured(msgConf.getString("loginMessage"));
        Messages.registerTitle = createTitle(getMsgString("registerTitle"));
        Messages.loginTitle = createTitle(getMsgString("loginTitle"));
        Messages.logoutMessage = getMsgString("logoutMessage");
        Messages.changePassError = getMsgString("changePassError");
        Messages.genericPremiumError = getMsgString("genericPremiumError");
        Messages.successfulChangePass = getMsgString("successfulChangePass");
            forceBackupServer = config.getBoolean("forceBackupServer");
            backupServer = config.getString("backupServer");
            backupServerEnabled = !backupServer.equals("");
            maxPremiumAccounts = config.getInt("maxPremiumAccounts");
            maxCrackedAccounts = config.getInt("maxCrackedAccounts");
            maxTotalAccounts = config.getInt("maxTotalAccounts");
            Messages.tooManyAccounts = getMsgString("tooManyAccounts");
            secondAttempt = tools.getColoured(config.getString("secondAttempt"));
            Config.secondAttemptEnabled = !secondAttempt.equals("");
            disconnectOnWrongPass = config.getBoolean("disconnectOnWrongPass");
            authTime = config.getInt("authTime");
            Messages.loginRegisterBossBar = getMsgString("loginRegisterBossBar");
            Messages.authTimeExceeded = getMsgString("authTimeExceeded");
            String database = config.getString("database").toLowerCase();
            switch (database){
                case "mysql":
                    storageType = StorageType.Mysql;
                    break;
                case "h2":
                    storageType = StorageType.H2;
                    break;
                default:
                    storageType = StorageType.H2;
                    break;
            }

            Configuration discord = config.getSection("discord");

            //Discord config
            Discord.bot_token = discord.getString("bot-token");
            Discord.compulsory_for = discord.getStringList("compulsory-for");
            Discord.enabled = discord.getBoolean("enabled");
            Discord.compulsory_for_enabled = Discord.enabled && 
                    AdiAuth.instance.getProxy().getPluginManager().getPlugin("LuckPerms") != null
                    && Discord.compulsory_for.size() != 0 ;
            Discord.loginButtonChannel = discord.getString("loginButtonChannel");
            Discord.activityStatus = discord.getString("activityStatus");
            Discord.roleToGive = discord.getString("roleToGive");
            Discord.roleToGiveEnabled = !(Discord.roleToGive.equals(""));
            Discord.buttonMessage = discord.getString("buttonMessage");
            Discord.buttonText = discord.getString("buttonText");

            Messages.discordRegisterMessage = getMsgString("discordRegisterMessage");
            Messages.discordLoginMessage = getMsgString("discordLoginMessage");
        }
         catch (Exception e){
            e.printStackTrace();
            tools.log(Level.SEVERE, "Config wasnt loaded properly!");
        }
    }


    private static String getMsgString(String name){
        return tools.getColoured(config.getString("messages."+name));
    }
    private static String getUncolouredMsg(String name){
        return config.getString("messages."+name);
    }
    private static Title createTitle(String msg){
        Title title = ProxyServer.getInstance().createTitle().title(
                new TextComponent(tools.getColoured(msg)));
        title.fadeIn(50);
        return title;
    }

    private static void saveDefaultConf(){
        File configFile = new File(AdiAuth.instance.getDataFolder(), "config.yml");
        if (!AdiAuth.instance.getDataFolder().exists()) {
            AdiAuth.instance.getDataFolder().mkdir();
        }
        File libFolder = new File(configFile.getParentFile().toString()+"/libs");
        if(!libFolder.exists()){
            libFolder.mkdir();
        }
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (InputStream is = AdiAuth.instance.getResourceAsStream("bungeeconfig.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create configuration file", e);
            }
        }
    }
}
