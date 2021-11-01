package ad1tya2.adiauth.Bungee.commands;

import ad1tya2.adiauth.Bungee.Config;
import ad1tya2.adiauth.Bungee.UserProfile;
import ad1tya2.adiauth.Bungee.data.storage;
import ad1tya2.adiauth.Bungee.utils.Uuids;
import ad1tya2.adiauth.Bungee.utils.tools;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static ad1tya2.adiauth.Bungee.utils.Uuids.getBackupServerUUID;

public class changeplayermode extends Command implements TabExecutor {

    public changeplayermode(){
        super("changeplayermode", "adiauth.admin", "changeplayerloginmode");
    }

    private static List<String> options = Arrays.asList("premium", "cracked");

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 2){
            sender.sendMessage("Wrong usage!, please use /changeplayermode <player> <mode ('premium' or 'cracked')>");
            return;
        }
        UserProfile player = storage.getPlayerMemory(args[0]);
        if(player == null){
            sender.sendMessage("Player does not exist.");
            return;
        }
        if(!options.contains(args[1])){
            sender.sendMessage("Invalid mode, enter either premium or cracked.");
            return;
        }
        boolean isPremium = args[1] == "premium";
        if(isPremium != player.isPremium()){
            if(!isPremium){
                player.premiumUuid = null;
            } else {
                Optional<UUID> uuid;

                if(Config.forceBackupServer){
                    uuid = Uuids.getBackupServerUUID(player.username);
                }
                else {
                    uuid = Uuids.getMojangUUid(player.username);
                    if (uuid == null) {
                        if (Config.backupServerEnabled) {
                            uuid = getBackupServerUUID(player.username);
                            tools.log(Level.WARNING, "&eUsing backup server for " + player.username);
                        }
                    }
                }

                if(uuid != null && uuid.isPresent()){
                    player.uuid = uuid.get();
                }
            }
            storage.updatePlayer(player);
        }
        sender.sendMessage("Successfully updated login mode for player: "+player.username);

    }


    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if(args.length == 1){
            return ProxyServer.getInstance().getPlayers()
                    .parallelStream()
                    .map(CommandSender::getName).filter(name -> name.startsWith(args[0])).collect(Collectors.toList());
        } else if(args.length == 2){
            return options;
        } else {
            return Collections.emptyList();
        }
    }
}
