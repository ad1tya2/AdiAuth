package ad1tya2.adiauth.Bungee.commands;

import ad1tya2.adiauth.Bungee.UserProfile;
import ad1tya2.adiauth.Bungee.data.storage;
import ad1tya2.adiauth.Bungee.utils.tools;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class checkuserdata extends Command {
    public checkuserdata(){
        super("checkuserdata", "adiauth.admin", "viewuserprofile", "checkplayer");
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length != 1){
            sender.sendMessage(
                    tools.getColoured("&2Invalid args\n " +
                            "&bUse /checkuserdata <playername>")
            );
            return;
        }
        String pName = args[0];
        UserProfile profile = storage.getPlayerMemory(pName);
        if(profile == null){
            sender.sendMessage(ChatColor.RED+"Invalid username!");
        }
        else {
            sender.sendMessage(profile.getDataFormatted());
        }

    }
}
