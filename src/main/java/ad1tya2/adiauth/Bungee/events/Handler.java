package ad1tya2.adiauth.Bungee.events;

import ad1tya2.adiauth.Bungee.AdiAuth;
import ad1tya2.adiauth.Bungee.Config;
import ad1tya2.adiauth.Bungee.commands.login;
import ad1tya2.adiauth.Bungee.data.servers;
import ad1tya2.adiauth.Bungee.data.storage;
import ad1tya2.adiauth.Bungee.utils.pluginMessaging;
import ad1tya2.adiauth.Bungee.utils.tools;
import ad1tya2.adiauth.Bungee.UserProfile;
import ad1tya2.adiauth.PluginMessages;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;


import java.lang.reflect.Field;

import java.util.logging.Level;

public class Handler implements Listener {
    @EventHandler(priority = 127)
    public void onPreLogin(PreLoginEvent event){
        if(event.isCancelled()){
            return;
        }
        event.registerIntent(AdiAuth.instance);

        AdiAuth.runAsync(new Runnable() {
                             @Override
                             public void run() {
                                 PendingConnection conn = event.getConnection();
                                 UserProfile profile = storage.getPlayerForLogin(conn.getName(), tools.getIp(conn.getSocketAddress()));
                                 if(profile == null){
                                     event.setCancelReason(tools.getColoured(
                                             "&cConnection has been cancelled due to internal server error."
                                     ));
                                     event.setCancelled(true);
                                     return;
                                 }
                                 conn.setUniqueId(profile.uuid);
                                 conn.setOnlineMode(profile.isPremium());
                                 event.completeIntent(AdiAuth.instance);
                         }
        });

    }

    @EventHandler(priority = 127)
    public void onLogin(LoginEvent event){
        try {
            InitialHandler handler = (InitialHandler) event.getConnection();
            UserProfile profile = storage.getPlayerDirect(handler.getName());
            Class handle = handler.getClass();
            Field uniqueId = handle.getDeclaredField("uniqueId");
            uniqueId.setAccessible(true);
            uniqueId.set(handler, profile.uuid);
        } catch (Exception e){
            e.printStackTrace();
            tools.log(Level.SEVERE, "Login unsuccessful!");
        }
    }



    @EventHandler
    public void serverConnect(ServerConnectEvent event){
        ProxiedPlayer p = event.getPlayer();
        UserProfile user = storage.getPlayerDirect(p.getName());
        if(user.isLogged()){
            event.setTarget(servers.getHubServer());
        }
        else {
            ServerInfo authServer = servers.getAuthServer();
            if(authServer == null){
                p.disconnect(Config.Messages.noServersAvailable);
                return;
            }
            event.setTarget(authServer);
            if(!user.isRegistered()){
                p.sendTitle(Config.Messages.registerTitle);
                p.sendMessage(Config.Messages.registerMessage);
            }else {
                p.sendTitle(Config.Messages.loginTitle);
                p.sendMessage(Config.Messages.loginMessage);
            }
            pluginMessaging.sendMessageBungee(user, PluginMessages.unLogged, authServer);
        }
    }

    @EventHandler(priority = -127)
    public void chatEvent(ChatEvent event){
        if(event.isCancelled() || !(event.getSender() instanceof ProxiedPlayer)){
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) event.getSender();
        UserProfile profile = storage.getPlayerDirect(p.getName());
        if(profile.isLogged()){
            return;
        }
        String msg = event.getMessage();
        for(String cmd: Config.whitelistedCommands){
            if(msg.startsWith(cmd)){
                return;
            }
        }
        event.setCancelled(true);
    }
}
