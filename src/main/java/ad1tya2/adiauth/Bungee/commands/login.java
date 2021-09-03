package ad1tya2.adiauth.Bungee.commands;

import ad1tya2.adiauth.Bungee.Config;
import ad1tya2.adiauth.Bungee.UserProfile;
import ad1tya2.adiauth.Bungee.data.storage;
import ad1tya2.adiauth.Bungee.events.discord;
import ad1tya2.adiauth.Bungee.utils.passwordUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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
        String pass = args[0];
        if(profile.isLogged()){
            sender.sendMessage(Config.Messages.alreadyLoggedIn);
        }
        else if(profile.password == null){
            sender.sendMessage(Config.Messages.loginNotRegistered);
        }
        else if(passwordUtils.comparePass(profile, pass)){
            if(profile.is2faLoginNeeded()){
                discord.discordLogin(profile, p);
            }
            else {
                profile.loggedInPlayer(p);
            }
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
}
