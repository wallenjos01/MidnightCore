package me.m1dnightninja.midnightcore.fabric.mixin;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.fabric.api.PermissionHelper;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class MixinPlayer {

    @Inject(method = "canUseGameMasterBlocks()Z", at=@At("HEAD"), cancellable = true)
    public void canUseCommandBlock(CallbackInfoReturnable<Boolean> cir) {

        if(MidnightCoreAPI.getInstance().getMainConfig().getBoolean("vanilla_permissions")) {

            Player pl = (Player) (Object) this;
            if(!(pl instanceof SharedSuggestionProvider)) return;

            cir.setReturnValue(PermissionHelper.checkOrOp((SharedSuggestionProvider) pl, "minecraft.adminblocks", 2));
            cir.cancel();
        }

    }

}
