package ad1tya2.adiauth.Bungee.utils;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

public class BossBar {
    public static void sendPacket(
            ProxiedPlayer player,
            DefinedPacket packet
    ) {
        if (player.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_9) {
            player.unsafe().sendPacket(packet);
        }
    }
    public static void sendTitleAndHealth(String title, float health, ProxiedPlayer p){
        net.md_5.bungee.protocol.packet.BossBar packet =
                new net.md_5.bungee.protocol.packet.BossBar(p.getUniqueId(), 0);
        packet.setTitle(ComponentSerializer.toString(new ComponentBuilder(title).create()));
        packet.setColor(1);
        packet.setDivision(0);
        packet.setHealth(health);
        packet.setFlags((byte) 0x0);
        sendPacket(p, packet);
    }
    public static void removeTitle(ProxiedPlayer p){
        sendPacket(p, new net.md_5.bungee.protocol.packet.BossBar(p.getUniqueId(), 1));
    }
}