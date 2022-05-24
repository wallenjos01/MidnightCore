package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DebugStickItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;

@Mixin(DebugStickItem.class)
public class MixinDebugStickItem {

    @Redirect(method="handleInteraction", at=@At(value = "INVOKE", target="Lnet/minecraft/world/entity/player/Player;canUseGameMasterBlocks()Z"))
    private boolean onUse(Player instance) {

        if(!MidnightCoreAPI.getInstance().getConfig().getBoolean("vanilla_permissions")) return instance.canUseGameMasterBlocks();

        FabricPlayer fpl = FabricPlayer.wrap((ServerPlayer) instance);
        return instance.canUseGameMasterBlocks() || fpl.hasPermission("minecraft.debug_stick");

    }

}
