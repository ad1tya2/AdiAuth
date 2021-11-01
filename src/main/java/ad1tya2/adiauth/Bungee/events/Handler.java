package ad1tya2.adiauth.Bungee.events;

import ad1tya2.adiauth.Bungee.AdiAuth;
import ad1tya2.adiauth.Bungee.Config;
import ad1tya2.adiauth.Bungee.UserProfile;
import ad1tya2.adiauth.Bungee.data.servers;
import ad1tya2.adiauth.Bungee.data.storage;
import ad1tya2.adiauth.Bungee.utils.pluginMessaging;
import ad1tya2.adiauth.Bungee.utils.tools;
import ad1tya2.adiauth.PluginMessages;
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.event.EventHandler;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.logging.Level;

public class Handler implements Listener {

    @EventHandler(priority = Byte.MAX_VALUE)
    public void onPreLogin(PreLoginEvent event){
        if(event.isCancelled()){
            return;
        }
        event.registerIntent(AdiAuth.instance);

        AdiAuth.runAsync(new Runnable() {
                             @Override
                             public void run() {
                                 PendingConnection conn = event.getConnection();
                                 ProxiedPlayer player = ProxyServer.getInstance().getPlayer(conn.getName());
                                 if(player != null && player.isConnected()){
                                     conn.disconnect(tools.getColoured(
                                             "&eAnother player with that name is already online!"
                                     ));
                                     event.setCancelled(true);
                                     return;
                                 }
                                 Optional<UserProfile> optional = storage.getPlayerForLogin(conn.getName(), tools.getIp(conn.getSocketAddress()));
                                 if(optional == null){
                                     conn.disconnect(Config.Messages.tooManyAccounts);
                                     event.setCancelled(true);
                                     return;
                                 }
                                 if(!optional.isPresent()){
                                     conn.disconnect(tools.getColoured(
                                             "&cConnection has been cancelled due to internal server error."
                                     ));
                                     event.setCancelled(true);
                                     return;
                                 }
                                 UserProfile profile = optional.get();
                                 if(profile.isLoginBeingProcessed()){
                                     conn.disconnect(Config.secondAttempt);
                                     event.setCancelled(true);
                                     profile.loginProcessCompleted();
                                     return;
                                 }
                                 profile.startLoginProcess();
                                 conn.setUniqueId(profile.uuid);
                                 conn.setOnlineMode(profile.isPremium());
                                 event.completeIntent(AdiAuth.instance);
                         }
        });

    }

    @EventHandler(priority = Byte.MIN_VALUE)
    public void onLogin(LoginEvent event){
        try {
            InitialHandler handler = (InitialHandler) event.getConnection();
            UserProfile profile = storage.getPlayerMemory(handler.getName());
            LoginResult loginResult = handler.getLoginProfile();
            Field uniqueId = handler.getClass().getDeclaredField("uniqueId");
            uniqueId.setAccessible(true);
            uniqueId.set(handler, profile.uuid);
            if(loginResult != null){
                loginResult.setId(profile.uuid.toString());
            }
        } catch (Exception e){
            e.printStackTrace();
            tools.log(Level.SEVERE, "Login unsuccessful!");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = 127)
    public void postLogin(PostLoginEvent event){
        UserProfile profile = storage.getPlayerMemory(event.getPlayer().getName());
            profile.loginProcessCompleted();
            if(profile.isPremium()) {
                profile.fullJoined = true;
            }
            storage.updatePlayer(profile);
    }

    @EventHandler
    public void serverConnect(ServerConnectEvent event){
        ProxiedPlayer p = event.getPlayer();
        UserProfile user = storage.getPlayerMemory(p.getName());
        if (!user.isLogged()) {
            ServerInfo authServer = servers.getAuthServer();
            if(authServer == null){
                p.disconnect(Config.Messages.noServersAvailable);
                return;
            }
            event.setTarget(authServer);
            if(!user.isRegistered()){
                p.sendTitle(Config.Messages.registerTitle);
                p.sendMessage(Config.Messages.registerMessage);
            } else {
                if(user.is2faLoginNeeded() && user.isPremium()){
                    discord.discordLogin(user, p);
                } else {
                    p.sendTitle(Config.Messages.loginTitle);
                    p.sendMessage(Config.Messages.loginMessage);
                }
            }
            user.startTitleTask(p);
            pluginMessaging.sendMessageBungee(user, PluginMessages.unLogged, authServer);
        }
    }

    @EventHandler(priority = -127)
    public void chatEvent(ChatEvent event){
        if(event.isCancelled() || !(event.getSender() instanceof ProxiedPlayer)){
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) event.getSender();
        UserProfile profile = storage.getPlayerMemory(p.getName());
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

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event){

    }

}
