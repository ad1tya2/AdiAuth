package ad1tya2.adiauth.Bungee.commands;

import ad1tya2.adiauth.Bungee.UserProfile;
import ad1tya2.adiauth.Bungee.data.storage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class deleteplayer extends Command {
    public deleteplayer(){
        super("deleteplayer", "adiauth.admin", "deleteuser");
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length != 1){
            sender.sendMessage(ChatColor.YELLOW+"Please use /deleteplayer <username>");
            return;
        }
        String username = args[0];
        UserProfile profile = storage.getPlayerMemory(username);
        if(profile == null){
            sender.sendMessage(ChatColor.YELLOW+"Player dosent exist");
        } else {
            profile.password = null;
            profile.endSession();
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(profile.uuid);
            if(p != null){
                p.disconnect(ChatColor.RED+"Your Account has been deleted!");
            }
            storage.deletePlayer(profile);
            sender.sendMessage("Successfully deleted "+profile.username);
        }
    }
}
