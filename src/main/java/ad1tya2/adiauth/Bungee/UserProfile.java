package ad1tya2.adiauth.Bungee;

import ad1tya2.adiauth.Bungee.data.servers;
import ad1tya2.adiauth.Bungee.events.discord;
import ad1tya2.adiauth.Bungee.utils.BossBar;
import ad1tya2.adiauth.Bungee.utils.pluginMessaging;
import ad1tya2.adiauth.Bungee.utils.tools;
import ad1tya2.adiauth.PluginMessages;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class UserProfile {
    public String username;
    public  UUID premiumUuid;
    public  UUID uuid;
    public  String password;
    public  String lastIp;
    private ScheduledFuture<?> titleTask;
    public boolean fullJoined = false;
    public String discordId;
    public boolean discordLoginPending = false;
    public Integer twoFactorCode;
    public Long lastLogin;
    //Full joined is set when a person completely logs into the server for the first time

    public long sessionEnd = 1L;
    boolean loggingIn = false;
    public boolean isPremium(){
        return premiumUuid != null;
    }

    public void startSession(ProxiedPlayer p){
        if(titleTask != null)
        {
            titleTask.cancel(true);
            BossBar.removeTitle(p);
            titleTask = null;
        }
        sessionEnd = System.currentTimeMillis() + Config.SessionTime*60000L;
        discordLoginPending = false;
        twoFactorCode = null;
    }

    public void endSession(){
        sessionEnd = 0L;
    }

    public boolean isLogged()
    {
        if(is2faLoginNeeded() && System.currentTimeMillis()>sessionEnd){
            return false;
        } else if(isPremium()){
            return true;
        } else {
            return System.currentTimeMillis()<sessionEnd;
        }

    }


    public boolean isRegistered(){
        return isPremium() || password != null;
    }

    public void startLoginProcess(){
        loggingIn = true;
    }

    public boolean isLoginBeingProcessed(){
        return loggingIn;
    }

    public void loginProcessCompleted(){
        loggingIn = false;
    }

    public void startTitleTask(ProxiedPlayer p){
        float maxTime = Config.authTime;
        final float[] timeLeft = {maxTime};
        titleTask = AdiAuth.executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if(timeLeft[0] > 0 && p.isConnected()) {
                    timeLeft[0]--;
                    BossBar.sendTitleAndHealth(
                            Config.Messages.loginRegisterBossBar.replaceAll("TIMELEFT",
                            String.valueOf((int) timeLeft[0])), timeLeft[0] /maxTime, p);
                } else {
                    p.disconnect(Config.Messages.authTimeExceeded);
                    discordLoginPending = false;
                }
            }
        }, 2, 1, TimeUnit.SECONDS);
    }

    public boolean is2faLoginNeeded(){
        return discordId != null || discord.isCompulsory(this);
    }

    public boolean is2faRegistered(){
        return discordId != null;
    }

    public void loggedInPlayer(ProxiedPlayer p){
        p.sendTitle(Config.Messages.loginAndRegisterSuccessTitle);
        p.sendMessage(Config.Messages.loginAndRegisterSuccess);
        startSession(p);
        Server server = p.getServer();
        if(server != null && Config.lobbies.contains(server.getInfo())){
            pluginMessaging.sendMessageBungee(p, PluginMessages.loggedIn, server.getInfo());
        }else {
            ServerInfo hub = servers.getHubServer();
            if(hub == null){
                p.disconnect(Config.Messages.noServersAvailable);
                return;
            }
            p.connect(hub);
        }
    }

    public String getTwoFactorCode(){
        twoFactorCode = twoFactorCode == null? (int)(Math.random()*9000)+1000: twoFactorCode;
        return String.valueOf(twoFactorCode);
    }

    public TextComponent getDataFormatted(){
        String data = "&e________________________________________"+
                "\n\n&2  Username: &b"+username+
                "\n&2  DiscordID: &b"+(discordId == null? "": discordId)+
                "\n&2  UUID: &b"+uuid +
                "\n&2  PremiumUUID: &b"+(premiumUuid == null? "": premiumUuid.toString());
        TextComponent component = new TextComponent();
        component.setText(tools.getColoured(data));
        TextComponent ipComponent = new TextComponent();
        ipComponent.setText(tools.getColoured("\n&2  Ip Address: &b"+ lastIp));
        ipComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, lastIp));
        ipComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to copy!").create()));
        component.addExtra(ipComponent);
        component.addExtra(tools.getColoured("\n&e________________________________________"));
        return component;
    }

}
