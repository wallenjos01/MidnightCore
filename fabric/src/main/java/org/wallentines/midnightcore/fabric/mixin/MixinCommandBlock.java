package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.CommandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;

@Mixin(CommandBlock.class)
public class MixinCommandBlock {

    @Redirect(method="use", at=@At(value = "INVOKE", target="Lnet/minecraft/world/entity/player/Player;canUseGameMasterBlocks()Z"))
    private boolean onUse(Player instance) {

        if(instance.level.isClientSide) return true;
        if(!MidnightCoreAPI.getInstance().getConfig().getBoolean("vanilla_permissions")) return instance.canUseGameMasterBlocks();

        FabricPlayer fpl = FabricPlayer.wrap((ServerPlayer) instance);
        return instance.canUseGameMasterBlocks() || fpl.hasPermission("minecraft.command_blocks");
    }
}
