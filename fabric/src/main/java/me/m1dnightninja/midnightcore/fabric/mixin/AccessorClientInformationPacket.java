package me.m1dnightninja.midnightcore.fabric.mixin;

import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundClientInformationPacket.class)
public interface AccessorClientInformationPacket {
    @Accessor
    String getLanguage();

    @Accessor
    int getViewDistance();
}
