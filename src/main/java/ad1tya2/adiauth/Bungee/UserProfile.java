package ad1tya2.adiauth.Bungee;

import ad1tya2.adiauth.Bungee.utils.BossBar;
import ad1tya2.adiauth.Bungee.utils.tools;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

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
    }

    public void endSession(){
        sessionEnd = 0L;
    }

    public boolean isLogged()
    {
        return isPremium() || System.currentTimeMillis()<sessionEnd;
    }

    public boolean isRegistered(){
        return password != null;
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
                }
            }
        }, 2, 1, TimeUnit.SECONDS);
    }
}
