package me.m1dnightninja.midnightcore.fabric.mixin;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ClientboundPlayerInfoPacket.class)
public interface AccessorPlayerListPacket {

    @Accessor("entries")
    List<ClientboundPlayerInfoPacket.PlayerUpdate> getEntries();

    @Accessor
    ClientboundPlayerInfoPacket.Action getAction();
}
