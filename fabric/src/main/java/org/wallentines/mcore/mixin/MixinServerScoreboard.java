package org.wallentines.mcore.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerScoreboard.class)
public class MixinServerScoreboard {

    @Redirect(method="onScoreChanged", at=@At(value="INVOKE", target="Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void redirectScoreChanged(PlayerList instance, Packet<?> packet) {
        mcore$broadcast(instance, packet);
    }

    @Redirect(method="onPlayerRemoved", at=@At(value="INVOKE", target="Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void redirectPlayerRemoved(PlayerList instance, Packet<?> packet) {
        mcore$broadcast(instance, packet);
    }

    @Redirect(method="onPlayerScoreRemoved", at=@At(value="INVOKE", target="Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void redirectPlayerScoreRemoved(PlayerList instance, Packet<?> packet) {
        mcore$broadcast(instance, packet);
    }

    @Redirect(method="setDisplayObjective", at=@At(value="INVOKE", target="Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void redirectDisplayObjective(PlayerList instance, Packet<?> packet) {
        mcore$broadcast(instance, packet);
    }

    @Redirect(method="addPlayerToTeam", at=@At(value="INVOKE", target="Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void redirectAddPlayer(PlayerList instance, Packet<?> packet) {
        mcore$broadcast(instance, packet);
    }

    @Redirect(method="removePlayerFromTeam", at=@At(value="INVOKE", target="Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void redirectRemovePlayer(PlayerList instance, Packet<?> packet) {
        mcore$broadcast(instance, packet);
    }

    @Redirect(method="onObjectiveChanged", at=@At(value="INVOKE", target="Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void redirectObjectiveChanged(PlayerList instance, Packet<?> packet) {
        mcore$broadcast(instance, packet);
    }

    @Redirect(method="onTeamAdded", at=@At(value="INVOKE", target="Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void redirectTeamAdd(PlayerList instance, Packet<?> packet) {
        mcore$broadcast(instance, packet);
    }

    @Redirect(method="onTeamChanged", at=@At(value="INVOKE", target="Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void redirectTeamChanged(PlayerList instance, Packet<?> packet) {
        mcore$broadcast(instance, packet);
    }

    @Redirect(method="onTeamRemoved", at=@At(value="INVOKE", target="Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void redirectTeamRemove(PlayerList instance, Packet<?> packet) {
        mcore$broadcast(instance, packet);
    }

    @Redirect(method="startTrackingObjective", at=@At(value="INVOKE", target="Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void redirectTrack(ServerGamePacketListenerImpl instance, Packet<?> packet) {
        ServerScoreboard sb = (ServerScoreboard) (Object) this;
        if(instance.getPlayer().getScoreboard() == sb) {
            instance.send(packet);
        }
    }

    @Redirect(method="stopTrackingObjective", at=@At(value="INVOKE", target="Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void redirectStopTrack(ServerGamePacketListenerImpl instance, Packet<?> packet) {
        ServerScoreboard sb = (ServerScoreboard) (Object) this;
        if(instance.getPlayer().getScoreboard() == sb) {
            instance.send(packet);
        }
    }

    @Unique
    private void mcore$broadcast(PlayerList players, Packet<?> packet) {
        ServerScoreboard sb = (ServerScoreboard) (Object) this;
        for(ServerPlayer spl : players.getPlayers()) {
            if(spl.getScoreboard() == sb) {
                spl.connection.send(packet);
            }
        }
    }

}
