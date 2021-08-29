package ad1tya2.adiauth.Spigot;

import org.bukkit.plugin.java.JavaPlugin;

public class AdiAuthSpigot extends JavaPlugin {
    public static JavaPlugin instance;
    @Override
    public void onEnable(){
        instance = this;
        getServer().getMessenger().registerIncomingPluginChannel(this, "adiauth:main", new pluginMsg());
        getServer().getPluginManager().registerEvents(new listener(), this);
    }
}
