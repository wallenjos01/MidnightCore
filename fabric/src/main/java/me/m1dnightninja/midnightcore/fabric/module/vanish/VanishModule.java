package me.m1dnightninja.midnightcore.fabric.module.vanish;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.common.module.vanish.AbstractVanishModule;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.event.PacketSendEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;

public class VanishModule extends AbstractVanishModule {

    @Override
    public boolean initialize(ConfigSection configuration) {

        Event.register(PacketSendEvent.class, this, event -> {

            MPlayer piq;
            if(event.getPacket() instanceof ClientboundAddPlayerPacket) {

                piq = MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(((ClientboundAddPlayerPacket) event.getPacket()).getPlayerId());

            } else if(event.getPacket() instanceof ClientboundPlayerInfoPacket) {

                piq = MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(((ClientboundPlayerInfoPacket) event.getPacket()).getEntries().get(0).getProfile().getId());

            } else {

                return;
            }

            if (isHiddenFor(piq, FabricPlayer.wrap(event.getPlayer()))) {
                event.setCancelled(true);
            }
        });

        return true;
    }

    @Override
    protected void doHidePlayer(MPlayer player) {

        for(ServerPlayer pl : MidnightCore.getServer().getPlayerList().getPlayers()) {

            MPlayer fpl = FabricPlayer.wrap(pl);
            if(hiddenFor.containsKey(player) && hiddenFor.get(player).contains(fpl)) continue;

            doHidePlayerFor(player, fpl);
        }
    }

    @Override
    protected void doHidePlayerFor(MPlayer player, MPlayer other) {

        ServerPlayer pp = ((FabricPlayer) player).getMinecraftPlayer();

        ServerPlayer op = ((FabricPlayer) other).getMinecraftPlayer();
        if(pp == null || op == null) return;

        op.connection.send(new ClientboundRemoveEntitiesPacket(pp.getId()));
        op.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, pp));

    }

    @Override
    protected void doShowPlayer(MPlayer player) {

        for(ServerPlayer pl : MidnightCore.getServer().getPlayerList().getPlayers()) {

            MPlayer fpl = FabricPlayer.wrap(pl);
            if(hiddenFor.containsKey(player) && hiddenFor.get(player).contains(fpl)) continue;

            doShowPlayerFor(player, fpl);
        }
    }

    @Override
    protected void doShowPlayerFor(MPlayer player, MPlayer other) {

        ServerPlayer pp = ((FabricPlayer) player).getMinecraftPlayer();

        ServerPlayer op = ((FabricPlayer) other).getMinecraftPlayer();
        if(pp == null || op == null) return;

        op.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, pp));

        if(pp.getLevel() != op.getLevel()) return;

        float headRot = pp.getYHeadRot();
        int rot = (int) headRot;
        if (headRot < (float) rot) rot -= 1;

        op.connection.send(new ClientboundAddPlayerPacket(pp));
        op.connection.send(new ClientboundSetEntityDataPacket(pp.getId(), pp.getEntityData(), true));
        op.connection.send(new ClientboundRotateHeadPacket(pp, (byte) ((rot * 256.0F) / 360.0F)));

    }
}
