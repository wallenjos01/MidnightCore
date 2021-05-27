package me.m1dnightninja.midnightcore.fabric.mixin;

import me.m1dnightninja.midnightcore.fabric.api.event.MenuCloseEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class MixinServerPlayer {

    @Inject(method = "doCloseContainer()V", at = @At("HEAD"))
    private void onClose(CallbackInfo ci) {

        ServerPlayer pl = (ServerPlayer) (Object) this;

        MenuCloseEvent ev = new MenuCloseEvent(pl, pl.containerMenu);
        Event.invoke(ev);
    }

}
