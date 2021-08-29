package ad1tya2.adiauth.Bungee.commands;

import ad1tya2.adiauth.Bungee.UserProfile;
import ad1tya2.adiauth.Bungee.data.storage;
import ad1tya2.adiauth.Bungee.utils.tools;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import javax.swing.*;

public class forcechangepass extends Command {
    public forcechangepass() {
        super("forcechangepass", "adiauth.admin", "adminchangepass");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length != 2){
            sender.sendMessage("&ePlease use /forcechangepass <username> <password>");
            return;
        }
        String username = args[0];
        String newPass = args[1];
        UserProfile profile = storage.getPlayerDirect(username);
        if(profile == null){
            sender.sendMessage("&ePlayer dosent exist");
        } else {
            profile.password = tools.getSha256(newPass);
            storage.updatePlayer(profile);
            sender.sendMessage("&2Successfully updated password of &b"+username);
        }
    }
}
