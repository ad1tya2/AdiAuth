package ad1tya2.adiauth.Bungee.commands;

import ad1tya2.adiauth.Bungee.UserProfile;
import ad1tya2.adiauth.Bungee.data.storage;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class unregister extends Command {
    public unregister(){
        super("unregister", "adiauth.admin", "forceunregister", "adminunregister");
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length != 1){
            sender.sendMessage("&ePlease use /unregister <username>");
            return;
        }
        String username = args[0];
        UserProfile profile = storage.getPlayerDirect(username);
        if(profile == null){
            sender.sendMessage("&ePlayer dosent exist");
        } else {
            profile.password = null;
            profile.endSession();
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(profile.uuid);
            if(p != null){
                p.disconnect("&cYou have been unregistered!");
            }
            storage.updatePlayer(profile);
            sender.sendMessage("&2Successfully unregistered &b"+username);
        }
    }
}
