package ad1tya2.adiauth.Bungee.commands;

import ad1tya2.adiauth.Bungee.Config;
import ad1tya2.adiauth.Bungee.UserProfile;
import ad1tya2.adiauth.Bungee.data.storage;
import ad1tya2.adiauth.Bungee.events.discord;
import ad1tya2.adiauth.Bungee.utils.tools;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class twofactor extends Command implements TabExecutor {
    private final static List<Cmd> cmds = new ArrayList<Cmd>();
    private final static List<String> normalCmds = new ArrayList<String>();
    private final static List<String> adminCmds = new ArrayList<String>();

    public twofactor() {
        super("2fa", null, "discord2fa", "2fauth", "2factor", "twofactor");

        registerCmd(new Cmd() {
            @Override
            public boolean isAdminCmd() {
                return false;
            }

            @Override
            public String getSubCommand() {
                return "login";
            }

            @Override
            public void onCmd(String arg, CommandSender sender) {
                if(!(sender instanceof ProxiedPlayer)){
                    sender.sendMessage("&cOnly Players can use this command!");
                    return;
                }
                ProxiedPlayer p = (ProxiedPlayer) sender;
                UserProfile profile = storage.getPlayerMemory(p.getName());
                if(profile.isLogged()){
                    p.sendMessage(Config.Messages.alreadyLoggedIn);
                } else if(!profile.is2faRegistered()){
                    p.sendMessage(discord.getRegisterMsg(profile));
                } else {
                    p.sendMessage(discord.getLoginMsg());
                }
            }
        });
        registerCmd(new Cmd() {
            @Override
            public boolean isAdminCmd() {
                return false;
            }

            @Override
            public String getSubCommand() {
                return "register";
            }

            @Override
            public void onCmd(String arg, CommandSender sender) {
                if(!(sender instanceof ProxiedPlayer)){
                    sender.sendMessage("&cOnly Players can use this command!");
                    return;
                }
                ProxiedPlayer p = (ProxiedPlayer) sender;
                UserProfile profile = storage.getPlayerMemory(p.getName());
                if(profile.is2faRegistered()){
                    p.sendMessage(Config.Messages.alreadyRegistered);
                } else {
                    p.sendMessage(discord.getRegisterMsg(profile));
                }
            }
        });

        registerCmd(new Cmd() {
            @Override
            public boolean isAdminCmd() {
                return false;
            }

            @Override
            public String getSubCommand() {
                return "disable";
            }

            @Override
            public void onCmd(String arg, CommandSender sender) {
                if(!(sender instanceof ProxiedPlayer)){
                    sender.sendMessage(tools.getColoured("&cOnly Players can use this command!"));
                    return;
                }
                ProxiedPlayer p = (ProxiedPlayer) sender;
                UserProfile profile = storage.getPlayerMemory(p.getName());
                if(!profile.is2faRegistered()){
                    p.sendMessage(Config.Messages.loginNotRegistered);
                } else {
                    if(discord.isCompulsory(profile)){
                        sender.sendMessage(tools.getColoured("&cCannot disable 2fa!"));
                    } else {
                        profile.discordId = null;
                        storage.updatePlayer(profile);
                        discord.removeRoleToGive(profile);
                        sender.sendMessage(tools.getColoured("&bSuccessfully disabled 2fa!"));
                    }
                }
            }
        });

        registerCmd(new Cmd() {
            @Override
            public boolean isAdminCmd() {
                return true;
            }

            @Override
            public String getSubCommand() {
                return "forcedisable";
            }

            @Override
            public void onCmd(String arg, CommandSender sender) {
                if(arg == null){
                    sender.sendMessage(ChatColor.RED+"Invalid arguments!");
                    return;
                }
                UserProfile profile = storage.getPlayerMemory(arg);
                if(profile == null){
                    sender.sendMessage(ChatColor.RED+"Player dosent exist");
                } else if(!profile.is2faRegistered()){
                    sender.sendMessage(ChatColor.YELLOW+"2fa isnt enabled for the player");
                }
                else {
                    profile.discordId = null;
                    storage.updatePlayer(profile);
                    discord.removeRoleToGive(profile);
                    sender.sendMessage(ChatColor.BLUE+"Successfully disabled 2fa for "+arg);
                }
            }
        });


        }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 1){
            sender.sendMessage("&cInvalid command!");
            return;
        }
        String mainCmd = args[0];
        for(Cmd cmd: cmds){
            if(cmd.getSubCommand().equals(mainCmd)) {
                if(cmd.isAdminCmd() && !sender.hasPermission("adiauth.admin")){
                    sender.sendMessage("&cYou do not have the permission to use this command.");
                    return;
                }
                if(args.length > 1)
                {
                    cmd.onCmd(args[1], sender);
                }
                else {
                    cmd.onCmd(null, sender);
                }
                return;
            }
        }
    }


    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if(args.length < 2) {
            if(sender.hasPermission("adiauth.admin")){
                return adminCmds;
            } else {
                return normalCmds;
            }
        } else {
            return Collections.emptyList();
        }
    }

    interface Cmd {
        boolean isAdminCmd();
        String getSubCommand();
        void onCmd(String arg, CommandSender sender);
    }

    private void registerCmd(Cmd cmd){
        cmds.add(cmd);
        if(cmd.isAdminCmd()){
            adminCmds.add(cmd.getSubCommand());
        } else {
            adminCmds.add(cmd.getSubCommand());
            normalCmds.add(cmd.getSubCommand());
        }
    }
}
