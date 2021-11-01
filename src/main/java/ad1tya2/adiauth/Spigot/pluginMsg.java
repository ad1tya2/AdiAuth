package ad1tya2.adiauth.Spigot;

import ad1tya2.adiauth.PluginMessages;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class pluginMsg implements PluginMessageListener{

    public static List<UUID> frozenPlayers = new ArrayList<UUID>();
    public static boolean freezePlayer = true;
    public static boolean blindness = true;
    public static boolean configured = false;

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        String[] rawMsg = new String(message, StandardCharsets.UTF_8).split("\\|");
        UUID uuid = UUID.fromString(rawMsg[0]);
        //Configure if not configured
        if(!configured) {
            String[] conf = rawMsg[1].split(",");
            for (String rawElement : conf) {
                String[] elements = rawElement.split("=");
                String key = elements[0];
                String value = elements[1];
                switch (key) {
                    case "freezePlayer":
                        freezePlayer = Boolean.parseBoolean(value);
                        break;
                    case "blindness":
                        blindness = Boolean.parseBoolean(value);
                        break;
                    default:
                        break;
                }
            }
            configured = true;
        }

        String msg = rawMsg[2];
        if(msg.equals(PluginMessages.loggedIn)){
            unfreezePlayer(uuid);
        }

        switch (msg){
            case PluginMessages.loggedIn:
                unfreezePlayer(uuid);
                break;
            case PluginMessages.unLogged:
                freezePlayer(uuid);
                break;
            default:
                break;
        }
    }

    public static void freezePlayer(UUID uuid){
        if(!frozenPlayers.contains(uuid)) {
            Player p = Bukkit.getPlayer(uuid);
            if(p != null) {

                if (blindness) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 10));
                }
                p.setAllowFlight(true);
                p.spigot().setCollidesWithEntities(false);
            }
            frozenPlayers.add(uuid);
        }
    }

    public static void unfreezePlayer(UUID uuid){
        frozenPlayers.remove(uuid);
        Player p = Bukkit.getPlayer(uuid);
        if(p != null) {
            unfreezePlayer(p);
        }
    }

    public static void unfreezePlayer(Player p){
        p.spigot().setCollidesWithEntities(true);
        frozenPlayers.remove(p.getUniqueId());
        if(blindness) {
            p.removePotionEffect(PotionEffectType.BLINDNESS);
        }
        if(freezePlayer){
            p.setAllowFlight(false);
        }
    }
}
