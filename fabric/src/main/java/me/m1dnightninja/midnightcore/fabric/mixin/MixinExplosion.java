package me.m1dnightninja.midnightcore.fabric.mixin;

import me.m1dnightninja.midnightcore.fabric.event.ExplosionEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Explosion.class)
public class MixinExplosion {

    @Shadow @Final private List<BlockPos> toBlow;

    @Shadow @Final @Nullable private Entity source;

    @Inject(method = "finalizeExplosion(Z)V", at=@At("HEAD"))
    private void onExplosionFinalized(boolean b, CallbackInfo inf) {

        ExplosionEvent event = new ExplosionEvent(toBlow, source);
        Event.invoke(event);

        if(event.isCancelled()) {
            event.getAffectedBlocks().clear();
        }
    }
}
