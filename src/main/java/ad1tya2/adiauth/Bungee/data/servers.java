package ad1tya2.adiauth.Bungee.data;

import ad1tya2.adiauth.Bungee.AdiAuth;
import ad1tya2.adiauth.Bungee.Config;
import net.md_5.bungee.api.Callback;

import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class servers {
    private static HashMap<String, Boolean> serverMap;

    public static void load(){
        serverMap = new HashMap<String, Boolean>();
    }

    public static ServerInfo getAuthServer(){
        ServerInfo leastPlayerServer = null;
        for(ServerInfo server:Config.auths){
            if(!isOnline(server)){
                continue;
            }
            if(leastPlayerServer == null){
                leastPlayerServer = server;
            }
            else if(leastPlayerServer.getPlayers().size() > server.getPlayers().size()){
                leastPlayerServer = server;
            }
        }
        return leastPlayerServer;
    }

    public static ServerInfo getHubServer(){
        ServerInfo leastPlayerServer = null;
        for(ServerInfo server: Config.lobbies){
            if(!isOnline(server)){
                continue;
            }
            if(leastPlayerServer == null){
                leastPlayerServer = server;
            }
            else if(leastPlayerServer.getPlayers().size() > server.getPlayers().size()){
                leastPlayerServer = server;
            }
        }
        return leastPlayerServer;
    }

    public static void serversStatusChecker(){
        AdiAuth.executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                List<ServerInfo> servers = new ArrayList<ServerInfo>();
                servers.addAll(Config.lobbies);
                servers.addAll(Config.auths);
                for(ServerInfo server: servers){
                    server.ping(new Callback<ServerPing>() {
                        @Override
                        public void done(ServerPing serverPing, Throwable throwable) {
                            boolean isOnline = (serverPing != null);
                            serverMap.put(
                                    server.getName(),
                                    isOnline);
                        }
                    });
                }
            }
        }, 1, Config.serverCheckInterval, TimeUnit.SECONDS);
    }


    public static boolean isOnline(ServerInfo server){
        return serverMap.get(server.getName());
    }
}
