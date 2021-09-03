package ad1tya2.adiauth.Bungee.events;

import ad1tya2.adiauth.Bungee.Config;
import ad1tya2.adiauth.Bungee.UserProfile;
import ad1tya2.adiauth.Bungee.data.storage;
import ad1tya2.adiauth.Bungee.utils.tools;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Button;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class discord {
    private static String dmLink, BOTNAME, CHANNELNAME, channelLink;
    private static ConcurrentHashMap<String, UserProfile> pendingRegistrations = new ConcurrentHashMap<String, UserProfile>();
    private static JDA client;
    private static Guild mainGuild;
    public static void load(){
        try {
            if (Config.Discord.enabled) {
                tools.loadLibrary(
                        "https://github.com/DV8FromTheWorld/JDA/releases/download/v4.3.0/JDA-4.3.0_277-withDependencies-min.jar",
                        "JDA 4.3.0_277");
                tools.log("&bLoading discord bot and creating messages!");
                JDABuilder builder = JDABuilder.createDefault(Config.Discord.bot_token);
                builder.setActivity(Activity.watching(Config.Discord.activityStatus));
                 client = builder.build();

                client.addEventListener(new ListenerAdapter(){
                    @Override
                    public void onMessageReceived(MessageReceivedEvent event)
                    {
                        if (event.isFromType(ChannelType.PRIVATE) )
                        {
                            String code = event.getMessage().getContentRaw();
                            UserProfile profile = pendingRegistrations.get(code);
                            UserProfile discordAuthorProfile = storage.getPlayerByDiscord(event.getAuthor().getId());
                            if(profile == null){
                                return;
                            }

                            if(profile.discordId != null || discordAuthorProfile != null){
                                profile.discordLoginPending = false;
                                event.getMessage().reply("You can only link one discord account to one minecraft account.").queue();
                                pendingRegistrations.remove(code);
                                return;
                            }

                            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(profile.username);
                            if(profile.discordLoginPending && code.equals(String.valueOf(profile.getTwoFactorCode())) && p != null){
                                profile.loggedInPlayer(p);
                                p.sendMessage(tools.getColoured("&22fa Successfully enabled!"));
                                profile.discordId = event.getAuthor().getId();
                                if(Config.Discord.roleToGiveEnabled){
                                    event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(Config.Discord.roleToGive)).queue();
                                }
                                event.getMessage().reply("2fa Successfully enabled!").queue();
                                pendingRegistrations.remove(code);
                                storage.updatePlayer(profile);
                            }
                        }
                    }

                    @Override
                    public void onButtonClick(ButtonClickEvent event){
                        if(event.getComponentId().equals("AdiAuthLogin")){
                            UserProfile profile = storage.getPlayerByDiscord(event.getUser().getId());
                            if(profile == null){
                                return;
                            }
                            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(profile.username);
                            if(profile.discordLoginPending && p != null){
                                profile.loggedInPlayer(p);
                                event.getInteraction().reply("Successful login!").setEphemeral(true).queue();
                            } else {
                                event.getInteraction().reply("Are you sure you need to use this?").setEphemeral(true).queue();
                            }
                        }
                    }
                });

                client.awaitReady();

                TextChannel loginButtonChannel = client.getTextChannelById(Config.Discord.loginButtonChannel);
                CHANNELNAME = "#"+loginButtonChannel.getName();
                List<Message> messages = loginButtonChannel.getIterableHistory().complete();
                boolean isLoginButtonExist = false;
                for(Message msg: messages){
                    if(msg.getButtonById("AdiAuthLogin") != null){
                        isLoginButtonExist = true;
                        channelLink = msg.getJumpUrl();
                        break;
                    }
                }
                if(!isLoginButtonExist) {
                    channelLink = loginButtonChannel.sendMessage(Config.Discord.buttonMessage).setActionRow(Button.success("AdiAuthLogin", Config.Discord.buttonText)).complete().getJumpUrl();
                }
                dmLink = "https://discord.com/channels/@me/"+ client.getSelfUser().getId();
                BOTNAME = client.getSelfUser().getAsTag();
                mainGuild = loginButtonChannel.getGuild();
                tools.log("&2Discord 2fa is now ready!");
            }
        } catch (Exception e){
            e.printStackTrace();
            tools.log(Level.SEVERE, "&cDiscord 2fa is not working.\n&eDisabling 2fa!");
            Config.Discord.enabled = false;
        }
    }

    public static void removeRoleToGive(UserProfile profile){
        if(Config.Discord.roleToGiveEnabled && profile.is2faRegistered()){
            mainGuild.addRoleToMember(mainGuild.getMemberById(profile.discordId), mainGuild.getRoleById(Config.Discord.roleToGive)).queue();
        }
    }


    public static void discordLogin(UserProfile profile, ProxiedPlayer p){
        if(profile.is2faRegistered()){
            p.sendMessage(getLoginMsg());
        } else {
            p.sendMessage(getRegisterMsg(profile));
        }
        profile.discordLoginPending = true;
    }

    public static boolean isCompulsory(UserProfile profile){
        if(Config.Discord.compulsory_for_enabled){
            User user = LuckPermsProvider.get().getUserManager().getUser(profile.uuid);
            if(user == null){
                return false;
            }
            Set<String> groups = user.getNodes().stream()
                    .filter(NodeType.INHERITANCE::matches)
                    .map(NodeType.INHERITANCE::cast)
                    .map(InheritanceNode::getGroupName)
                    .collect(Collectors.toSet());
            for(String group: groups){
                if(Config.Discord.compulsory_for.contains(group)){
                    return true;
                }
            }
        }
        return false;
    }

    public static String getLoginMsg(){
        return Config.Messages.discordLoginMessage.replace("THISLINK", channelLink).replace("CHANNELNAME", CHANNELNAME);
    }

    public static String getRegisterMsg(UserProfile profile){
        profile.discordLoginPending = true;
        pendingRegistrations.put(profile.getTwoFactorCode(), profile);
        return Config.Messages.discordRegisterMessage.replace("CODE", String.valueOf(profile.getTwoFactorCode())).replace("BOTNAME", BOTNAME).replace("THISLINK", dmLink);
    }
}
