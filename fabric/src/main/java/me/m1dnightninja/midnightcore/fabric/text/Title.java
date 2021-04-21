package me.m1dnightninja.midnightcore.fabric.text;

import me.m1dnightninja.midnightcore.api.text.AbstractTitle;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.util.ConversionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class Title extends AbstractTitle {

    private final Component mcComponent;

    public Title(MComponent text, TitleOptions options) {
        super(text, options);

        mcComponent = ConversionUtil.toMinecraftComponent(text);
    }

    @Override
    public void sendToPlayer(UUID u) {

        ServerPlayer player = MidnightCore.getServer().getPlayerList().getPlayer(u);
        if(player == null) return;

        if(getOptions().clear) {
            ClientboundSetTitlesPacket packet = new ClientboundSetTitlesPacket(ClientboundSetTitlesPacket.Type.CLEAR, null, 0, 0, 0);
            player.connection.send(packet);
        }

        ClientboundSetTitlesPacket packet = new ClientboundSetTitlesPacket(
                options.subtitle ? ClientboundSetTitlesPacket.Type.SUBTITLE : ClientboundSetTitlesPacket.Type.TITLE,
                mcComponent,
                options.fadeIn,
                options.stay,
                options.fadeOut
        );

        player.connection.send(packet);
    }

    @Override
    public void sendToPlayers(Iterable<UUID> u) {

        if(getOptions().clear) {
            ClientboundSetTitlesPacket packet = new ClientboundSetTitlesPacket(ClientboundSetTitlesPacket.Type.CLEAR, null, 0, 0, 0);
            for(UUID u1 : u) {

                ServerPlayer player = MidnightCore.getServer().getPlayerList().getPlayer(u1);
                if(player == null) continue;

                player.connection.send(packet);
            }
        }

        ClientboundSetTitlesPacket packet = new ClientboundSetTitlesPacket(
                options.subtitle ? ClientboundSetTitlesPacket.Type.SUBTITLE : ClientboundSetTitlesPacket.Type.TITLE,
                mcComponent,
                options.fadeIn,
                options.stay,
                options.fadeOut
        );

        for(UUID u1 : u) {

            ServerPlayer player = MidnightCore.getServer().getPlayerList().getPlayer(u1);
            if(player == null) continue;

            player.connection.send(packet);
        }
    }
}
