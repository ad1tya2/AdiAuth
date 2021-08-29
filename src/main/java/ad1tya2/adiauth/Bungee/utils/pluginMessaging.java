package ad1tya2.adiauth.Bungee.utils;

import ad1tya2.adiauth.Bungee.Config;
import ad1tya2.adiauth.Bungee.UserProfile;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class pluginMessaging
{
    public static void sendMessageBungee(UserProfile user, String msg, ServerInfo server){
        sendMessageBungee(user.uuid, msg, server);
    }

    public static void sendMessageBungee(ProxiedPlayer p, String msg, ServerInfo server){
        sendMessageBungee(p.getUniqueId(), msg, server);
    }

    public static void sendMessageBungee(UUID p, String msg, ServerInfo server){
        server.sendData("adiauth:main",
                (p.toString() +"|"+Config.pluginMsgConf+"|"+msg).getBytes());
    }


}
