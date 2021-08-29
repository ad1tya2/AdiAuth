package ad1tya2.adiauth.Bungee.commands;

import ad1tya2.adiauth.Bungee.AdiAuth;
import ad1tya2.adiauth.Bungee.utils.tools;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class reload extends Command {

    public reload() {
        super("adireload", null, "aareload", "authreload");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        AdiAuth.load();
        sender.sendMessage(tools.getColoured("&bAdiAuth was successfully Reloaded!"));
    }
}
