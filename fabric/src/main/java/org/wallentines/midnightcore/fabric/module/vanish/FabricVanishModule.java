package org.wallentines.midnightcore.fabric.module.vanish;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.skin.SkinModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.common.module.vanish.AbstractVanishModule;
import org.wallentines.midnightcore.fabric.event.player.PacketSendEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerJoinEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerLeaveEvent;
import org.wallentines.midnightcore.fabric.module.skin.FabricSkinModule;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.ModuleInfo;

public class FabricVanishModule extends AbstractVanishModule {

    @Override
    protected void doVanish(MPlayer player, MPlayer observer) {
        if(observer.equals(player) || observer.isOffline()) return;

        ServerPlayer op = FabricPlayer.getInternal(observer);
        ServerPlayer sp = FabricPlayer.getInternal(player);

        op.connection.send(new ClientboundRemoveEntitiesPacket(sp.getId()));
        op.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, sp));
    }

    @Override
    protected void doReveal(MPlayer player, MPlayer observer) {

        if(observer.equals(player) || observer.isOffline()) return;
        ServerPlayer op = FabricPlayer.getInternal(observer);
        ServerPlayer sp = FabricPlayer.getInternal(player);

        op.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, sp));

        if(sp.getLevel() != op.getLevel()) return;

        float headRot = sp.getYHeadRot();
        int rot = (int) headRot;
        if (headRot < (float) rot) rot -= 1;

        op.connection.send(new ClientboundAddPlayerPacket(sp));
        op.connection.send(new ClientboundSetEntityDataPacket(sp.getId(), sp.getEntityData(), true));
        op.connection.send(new ClientboundRotateHeadPacket(sp, (byte) ((rot * 256.0F) / 360.0F)));

    }

    @Override
    protected void registerEvents() {

        Event.register(PlayerJoinEvent.class, this, event -> {

            FabricPlayer fp = FabricPlayer.wrap(event.getPlayer());
            if(isVanished(fp)) {
                if (hideMessages) event.setJoinMessage((Component) null);
                for(MPlayer pl : MidnightCoreAPI.getInstance().getPlayerManager()) {
                    doVanish(fp, pl);
                }
            }
        });

        Event.register(PlayerLeaveEvent.class, this, event -> {

            FabricPlayer fp = FabricPlayer.wrap(event.getPlayer());
            if(isVanished(fp)) {
                if (hideMessages) event.setLeaveMessage(null);
            }
        });

        Event.register(PacketSendEvent.class, this, event -> {

            if(event.getPacket() instanceof ClientboundAddPlayerPacket pck) {
                MPlayer mp = MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(pck.getPlayerId());
                if(isVanished(mp) || isVanishedFor(mp, FabricPlayer.wrap(event.getPlayer()))) event.setCancelled(true);
            }
        });

    }

    public static final ModuleInfo<MidnightCoreAPI> MODULE_INFO = new ModuleInfo<>(FabricVanishModule::new, ID, DEFAULTS);
}
