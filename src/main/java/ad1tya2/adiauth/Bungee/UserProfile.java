package ad1tya2.adiauth.Bungee;

import java.util.UUID;

public class UserProfile {
    public String username;
    public  UUID premiumUuid;
    public  UUID uuid;
    public  String password;
    public  String lastIp;
    public long sessionEnd = 1L;
    boolean loggingIn = false;
    public boolean isPremium(){
        return premiumUuid != null;
    }

    public void startSession(){
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




}
