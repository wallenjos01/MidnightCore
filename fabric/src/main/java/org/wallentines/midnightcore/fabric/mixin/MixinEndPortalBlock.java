package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.midnightcore.fabric.level.DynamicLevelContext;

@Mixin(EndPortalBlock.class)
public class MixinEndPortalBlock {

    private Level midnightcore_level;

    @Inject(method="entityInside", at=@At("HEAD"))
    private void onEntityTeleport(BlockState blockState, Level level, BlockPos blockPos, Entity entity, CallbackInfo ci) {
        midnightcore_level = level;
    }

    @ModifyVariable(method="entityInside", at=@At(value="STORE"), ordinal = 0)
    private ResourceKey<Level> onNetherPortal(ResourceKey<Level> value) {

        Level level = midnightcore_level;
        midnightcore_level = null;

        if(level instanceof DynamicLevelContext.DynamicLevel lvl) {
            ResourceKey<Level> end = lvl.getEnd();
            return end == level.dimension() ? lvl.getRoot() : end;
        }
        return value;
    }

}
