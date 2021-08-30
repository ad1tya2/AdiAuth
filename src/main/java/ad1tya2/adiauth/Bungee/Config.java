package ad1tya2.adiauth.Bungee;

import ad1tya2.adiauth.Bungee.utils.tools;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Config {
    public static int SessionTime = 120;

    public static List<ServerInfo> lobbies = new ArrayList<ServerInfo>();
    public static List<ServerInfo> auths = new ArrayList<ServerInfo>();
    public static List<String> whitelistedCommands;

    public static String pluginMsgConf;
    public static int serverCheckInterval = 30,
            maxPremiumAccounts, maxCrackedAccounts, maxTotalAccounts;
    public static String backupServer, secondAttempt;
    public static boolean forceBackupServer, convertOldCrackedToPremium, backupServerEnabled, secondAttemptEnabled;
    public static class Messages {
        public static String registerError, loginAndRegisterSuccess, alreadyLoggedIn, loginNotRegistered,
                loginWrongPass, loginError, alreadyRegistered, noServersAvailable, registerMessage,
                loginMessage, logoutMessage, changePassError, genericPremiumError, successfulChangePass,
                tooManyAccounts;
        public static Title loginAndRegisterSuccessTitle, registerTitle, loginTitle;
    }
    public  enum MYSQL {
        username,
        password,
        database,
        host;
        public String value;
    }


    public static Configuration config;


    public static void load() {
        saveDefaultConf();
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(AdiAuth.instance.getDataFolder(), "config.yml"));
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
            convertOldCrackedToPremium = config.getBoolean("convertOldCrackedToPremium");
            maxPremiumAccounts = config.getInt("maxPremiumAccounts");
            maxCrackedAccounts = config.getInt("maxCrackedAccounts");
            maxTotalAccounts = config.getInt("maxTotalAccounts");
            Messages.tooManyAccounts = getMsgString("tooManyAccounts");
            secondAttempt = tools.getColoured(config.getString("secondAttempt"));
            Config.secondAttemptEnabled = !secondAttempt.equals("");

        }
         catch (Exception e){
            e.printStackTrace();
            tools.log(Level.SEVERE, "Config wasnt loaded properly!");
        }


    }


    private static String getMsgString(String name){
        return tools.getColoured(config.getString("messages."+name));
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
