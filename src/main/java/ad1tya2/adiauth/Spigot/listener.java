package ad1tya2.adiauth.Spigot;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.entity.EntityMountEvent;

public class listener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player p = event.getPlayer();
        if(isFrozen(p)){
            if(pluginMsg.blindness){
                 p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 10));
            }
            if(pluginMsg.freezePlayer){
                p.setAllowFlight(true);
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event){
        pluginMsg.unfreezePlayer(event.getPlayer());
    }



    //Cancel all events
    @EventHandler
    public void onMove(PlayerMoveEvent event){
        cancelIfFreezePlayers(event, event.getPlayer());
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        cancel(event, event.getPlayer());
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        cancel(event, event.getPlayer());
    }
    @EventHandler
    public void onDamage(EntityDamageEvent event){
        cancel(event, event.getEntity());
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        cancel(event, event.getPlayer());
    }
    @EventHandler
    public void onPreProccess(PlayerCommandPreprocessEvent event){
        cancel(event, event.getPlayer());
    }
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        cancel(event, event.getPlayer());
    }
    @EventHandler
    public void onpickup(PlayerPickupItemEvent event){
        cancel(event, event.getPlayer());
    }
    @EventHandler
    public void ondrop(PlayerDropItemEvent event){
        cancel(event, event.getPlayer());
    }
    @EventHandler
    public void x(PlayerItemHeldEvent event){
        cancel(event, event.getPlayer());
    }
    @EventHandler
    public void x(PlayerItemDamageEvent event){
        cancel(event, event.getPlayer());
    }
    @EventHandler
    public void x(EntityMountEvent event){
        cancelIfFreezePlayers(event, event.getEntity());
    }
    @EventHandler
    public void x(InventoryClickEvent event){
        cancel(event, event.getWhoClicked());
    }
    @EventHandler
    public void x(InventoryOpenEvent event){
        cancel(event, event.getPlayer());
    }
    @EventHandler
    public void x(InventoryDragEvent event){
        cancel(event, event.getWhoClicked());
    }
    @EventHandler
    public void x(FoodLevelChangeEvent event){
        cancel(event, event.getEntity());
    }
    @EventHandler
    public void x(EntityTargetLivingEntityEvent event){
        cancel(event, event.getTarget());
    }


    public void cancelIfFreezePlayers(Cancellable event, Player p){
        if(pluginMsg.freezePlayer){
            cancel(event, p);
        }
    }
    public void cancelIfFreezePlayers(Cancellable event, Entity e){
        if(pluginMsg.freezePlayer){
            cancel(event, e);
        }
    }
    public void cancel(Cancellable event, Player p){
        if(isFrozen(p)){
            event.setCancelled(true);
        }
    }

    public void cancel(Cancellable event, Entity e){
        if(e instanceof Player){
            cancel(event, (Player) e);
        }
    }
    public static boolean isFrozen(Player p){
        return pluginMsg.frozenPlayers.contains(p.getUniqueId());
    }
}
