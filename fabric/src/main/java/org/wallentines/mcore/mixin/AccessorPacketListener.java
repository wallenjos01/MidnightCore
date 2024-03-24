package org.wallentines.mcore.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerCommonPacketListenerImpl.class)
public interface AccessorPacketListener {

    @Accessor("server")
    MinecraftServer getMinecraftServer();

}
