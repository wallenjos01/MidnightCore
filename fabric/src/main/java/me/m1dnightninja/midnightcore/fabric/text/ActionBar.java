package me.m1dnightninja.midnightcore.fabric.text;

import me.m1dnightninja.midnightcore.api.text.AbstractActionBar;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.util.ConversionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class ActionBar extends AbstractActionBar {

    private final Component mcComponent;

    public ActionBar(MComponent text, ActionBarOptions options) {
        super(text, options);
        mcComponent = ConversionUtil.toMinecraftComponent(text);
    }

    @Override
    public void sendToPlayer(UUID u) {

        ServerPlayer player = MidnightCore.getServer().getPlayerList().getPlayer(u);
        if(player == null) return;

        ClientboundSetTitlesPacket packet = new ClientboundSetTitlesPacket(
                ClientboundSetTitlesPacket.Type.ACTIONBAR,
                mcComponent,
                options.fadeIn,
                options.stay,
                options.fadeOut
        );

        player.connection.send(packet);
    }

    @Override
    public void sendToPlayers(Iterable<UUID> u) {

        ClientboundSetTitlesPacket packet = new ClientboundSetTitlesPacket(
                ClientboundSetTitlesPacket.Type.ACTIONBAR,
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
