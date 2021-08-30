package ad1tya2.adiauth.Bungee.commands;

import ad1tya2.adiauth.Bungee.Config;
import ad1tya2.adiauth.Bungee.UserProfile;
import ad1tya2.adiauth.Bungee.data.storage;
import ad1tya2.adiauth.Bungee.utils.tools;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class changepass extends Command {

    public changepass() {
        super("changepass", null, "changepassword", "passchange");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length != 2 || !(sender instanceof ProxiedPlayer)){
            sender.sendMessage(Config.Messages.changePassError);
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) sender;
        UserProfile profile = storage.getPlayerMemory(p.getName());
        if(profile.isPremium()){
            p.sendMessage(Config.Messages.genericPremiumError);
        }
        String oldPass = tools.getSha256(args[0]);
        String newPass = tools.getSha256(args[1]);

        if(oldPass != profile.password){
            p.sendMessage(Config.Messages.changePassError);
        } else if(newPass == oldPass){
            p.sendMessage(Config.Messages.successfulChangePass);
        } else {
            profile.password = newPass;
            storage.updatePlayer(profile);
            p.sendMessage(Config.Messages.successfulChangePass);
        }
    }
}
