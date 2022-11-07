package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.midnightcore.fabric.event.entity.EntityLoadedEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerLoadChunkEvent;
import org.wallentines.midnightlib.event.Event;

@Mixin(ChunkMap.class)
public class MixinChunkMap {

    @Inject(method="addEntity", at=@At("HEAD"))
    private void onAddEntity(Entity entity, CallbackInfo ci) {

        Event.invoke(new EntityLoadedEvent(entity));
    }

    @Inject(method="playerLoadedChunk", at=@At("HEAD"))
    private void onPlayerLoad(ServerPlayer serverPlayer, MutableObject<ClientboundLevelChunkWithLightPacket> mutableObject, LevelChunk levelChunk, CallbackInfo ci) {

        Event.invoke(new PlayerLoadChunkEvent(levelChunk, serverPlayer));
    }

}
