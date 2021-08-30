package ad1tya2.adiauth.Bungee.commands;

import ad1tya2.adiauth.Bungee.Config;
import ad1tya2.adiauth.Bungee.UserProfile;
import ad1tya2.adiauth.Bungee.data.storage;

import ad1tya2.adiauth.Bungee.utils.tools;
import net.md_5.bungee.api.CommandSender;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;


public class register extends Command {


    public register() {
        super("register", null, "reg");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length != 1 || !(sender instanceof ProxiedPlayer)){
            sender.sendMessage(Config.Messages.registerError);
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) sender;
        UserProfile profile = storage.getPlayerMemory(p.getName());
        String pass = tools.getSha256(args[0]);
        if(profile.isPremium()){
            sender.sendMessage(Config.Messages.alreadyRegistered);
        }
        else if(profile.password != null){
            sender.sendMessage(Config.Messages.alreadyRegistered);
        }
        else {
            profile.password = pass;
            storage.updatePlayer(profile);
            p.sendTitle(Config.Messages.loginAndRegisterSuccessTitle);
            p.sendMessage(Config.Messages.loginAndRegisterSuccess);
            profile.startSession();
            login.loggedInPlayer(p);
        }
    }
}
