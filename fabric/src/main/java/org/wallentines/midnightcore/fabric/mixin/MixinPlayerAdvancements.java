package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.advancements.Advancement;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.wallentines.midnightcore.fabric.event.player.PlayerMakeAdvancementEvent;
import org.wallentines.midnightlib.event.Event;

@Mixin(PlayerAdvancements.class)
public class MixinPlayerAdvancements {

    @Shadow private ServerPlayer player;

    @Inject(method="award", at=@At("RETURN"))
    private void onAward(Advancement advancement, String string, CallbackInfoReturnable<Boolean> cir) {

        PlayerMakeAdvancementEvent event = new PlayerMakeAdvancementEvent(player, advancement);
        Event.invoke(event);

    }


}
