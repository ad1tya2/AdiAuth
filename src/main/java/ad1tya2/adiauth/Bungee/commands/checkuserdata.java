package ad1tya2.adiauth.Bungee.commands;

import ad1tya2.adiauth.Bungee.UserProfile;
import ad1tya2.adiauth.Bungee.data.storage;
import ad1tya2.adiauth.Bungee.utils.tools;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class checkuserdata extends Command {

    static String zeroTo255
            = "(\\d{1,2}|(0|1)\\"
            + "d{2}|2[0-4]\\d|25[0-5])";
    static String regex
            = zeroTo255 + "\\."
            + zeroTo255 + "\\."
            + zeroTo255 + "\\."
            + zeroTo255;
     static Pattern p = Pattern.compile(regex);


    public checkuserdata(){
        super("checkuserdata", "adiauth.admin", "viewuserprofile", "checkplayer");
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length != 1){
            sender.sendMessage(
                    tools.getColoured("&2Invalid args\n " +
                            "&bUse /checkuserdata <playername Or Ip> ")
            );
            return;
        }
        String pName = args[0];
        UserProfile profile = storage.getPlayerMemory(pName);
        if(profile == null){
            List<UserProfile> profiles;
            if(isValidIPAddress(pName) && (profiles = storage.getProfilesByIp(pName)) != null){
             for(UserProfile ipProfile: profiles){
                 sender.sendMessage(ipProfile.getDataFormatted());
             }
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid username or ip!");
            }
        }
        else {
            sender.sendMessage(profile.getDataFormatted());
        }

    }

    private static boolean isValidIPAddress(String ip)
    {
        return p.matcher(ip).matches();
    }
}
