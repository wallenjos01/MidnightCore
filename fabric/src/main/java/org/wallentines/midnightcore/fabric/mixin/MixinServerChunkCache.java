package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkCache.class)
public abstract class MixinServerChunkCache {

    @Shadow public abstract Level getLevel();

    @Inject(method="save", at=@At("HEAD"), cancellable = true)
    private void onSave(boolean b, CallbackInfo ci) {

        if(getLevel().noSave()) {
            ci.cancel();
        }
    }

}
