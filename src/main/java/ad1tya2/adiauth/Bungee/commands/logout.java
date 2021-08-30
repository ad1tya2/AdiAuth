package ad1tya2.adiauth.Bungee.commands;

import ad1tya2.adiauth.Bungee.Config;
import ad1tya2.adiauth.Bungee.UserProfile;
import ad1tya2.adiauth.Bungee.data.storage;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class logout extends Command {
    public logout() {
        super("logout");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)){
            sender.sendMessage("You cannot use this command!");
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) sender;
        UserProfile profile = storage.getPlayerMemory(p.getName());
        profile.endSession();
        p.disconnect(Config.Messages.logoutMessage);
    }
}
