package me.m1dnightninja.midnightcore.fabric.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerPlayer.class)
public interface AccessorServerPlayer {
    @Invoker
    void callNextContainerCounter();

    @Accessor
    int getContainerCounter();
}