package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.midnightcore.fabric.event.entity.EntityLoadedEvent;
import org.wallentines.midnightlib.event.Event;

@Mixin(ChunkMap.class)
public class MixinChunkMap {

    @Inject(method="addEntity", at=@At("HEAD"))
    private void onAddEntity(Entity entity, CallbackInfo ci) {

        Event.invoke(new EntityLoadedEvent(entity));
    }

}
