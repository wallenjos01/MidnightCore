package me.m1dnightninja.midnightcore.fabric.mixin;

import me.lucko.fabric.api.permissions.v0.Permissions;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
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

            cir.setReturnValue(Permissions.check(pl, "minecraft.adminblocks"));
            cir.cancel();
        }

    }

}
