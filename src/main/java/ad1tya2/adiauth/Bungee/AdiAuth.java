package ad1tya2.adiauth.Bungee;

import ad1tya2.adiauth.Bungee.commands.*;
import ad1tya2.adiauth.Bungee.data.database;
import ad1tya2.adiauth.Bungee.data.servers;
import ad1tya2.adiauth.Bungee.data.storage;
import ad1tya2.adiauth.Bungee.events.Handler;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

public final class AdiAuth extends Plugin {
    public static Plugin instance;
    private static List<String> sensitiveCommands = Arrays.asList("/login", "/l", "/register", "/reg");
    public static ScheduledThreadPoolExecutor executor;
    @Override
    public void onEnable() {
        executor = new ScheduledThreadPoolExecutor(4);
        // Plugin startup logic
        instance = this;

        Config.load();
        database.load();
        storage.load();
        servers.load();

        getProxy().getPluginManager().registerListener(this, new Handler());
        getProxy().getLogger().setFilter(new Filter() {
            @Override
            public boolean isLoggable(LogRecord record) {
                String msg = record.getMessage();
                if(msg != null){
                    for(String cmd: sensitiveCommands){
                        if(msg.contains(cmd)){
                            return false;
                        }
                    }
                }
                return true;
            }
        });

        //Load commands
        getProxy().getPluginManager().registerCommand(this, new register());
        getProxy().getPluginManager().registerCommand(this, new login());
        getProxy().getPluginManager().registerCommand(this, new reload());
        getProxy().getPluginManager().registerCommand(this, new logout());
        getProxy().getPluginManager().registerCommand(this, new changepass());
        getProxy().getPluginManager().registerCommand(this, new unregister());
        getProxy().getPluginManager().registerCommand(this, new forcechangepass());
        servers.serversStatusChecker();

    }

    public static void reload(){
        Config.load();
        storage.load();
        servers.load();
    }

    public static void runAsync(Runnable task){
        executor.execute(task);
    }

    @Override
    public void onDisable() {
        database.close();
        executor.shutdownNow();
        // Plugin shutdown logic
    }
}
