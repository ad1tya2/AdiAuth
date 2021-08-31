package ad1tya2.adiauth.Bungee.commands;

import ad1tya2.adiauth.Bungee.Config;
import ad1tya2.adiauth.Bungee.UserProfile;
import ad1tya2.adiauth.Bungee.data.servers;
import ad1tya2.adiauth.Bungee.data.storage;
import ad1tya2.adiauth.Bungee.utils.pluginMessaging;
import ad1tya2.adiauth.Bungee.utils.tools;
import ad1tya2.adiauth.PluginMessages;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Command;

public class login extends Command {
    public login() {
        super("login", null, "l");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length != 1 || !(sender instanceof ProxiedPlayer)){
            sender.sendMessage(Config.Messages.loginError);
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) sender;
        UserProfile profile = storage.getPlayerMemory(p.getName());
        String pass = tools.getSha256(args[0]);
        if(profile.isLogged()){
            sender.sendMessage(Config.Messages.alreadyLoggedIn);
        }
        else if(profile.password == null){
            sender.sendMessage(Config.Messages.loginNotRegistered);
        }
        else if(profile.password.equals(pass)){
            p.sendTitle(Config.Messages.loginAndRegisterSuccessTitle);
            p.sendMessage(Config.Messages.loginAndRegisterSuccess);
            profile.startSession(p);
            loggedInPlayer(p);
        }
        else {
            if(Config.disconnectOnWrongPass){
                p.disconnect(Config.Messages.loginWrongPass);
            }
            else {
                p.sendMessage(Config.Messages.loginWrongPass);
            }
        }
    }

    public static void loggedInPlayer(ProxiedPlayer p){
        Server server = p.getServer();
        if(server != null && Config.lobbies.contains(server.getInfo())){
            pluginMessaging.sendMessageBungee(p, PluginMessages.loggedIn, server.getInfo());
        }else {
            ServerInfo hub = servers.getHubServer();
            if(hub == null){
                p.disconnect(Config.Messages.noServersAvailable);
                return;
            }
            p.connect(hub);
        }

    }
}
