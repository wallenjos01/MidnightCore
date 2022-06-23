package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.wallentines.midnightcore.fabric.command.ServerSideArgumentInfo;

@Mixin(ClientboundCommandsPacket.ArgumentNodeStub.class)
public class MixinClientboundCommandsPacket {

    @ModifyArg(
            method="serializeCap(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/commands/synchronization/ArgumentTypeInfo;Lnet/minecraft/commands/synchronization/ArgumentTypeInfo$Template;)V",
            at=@At(value="INVOKE", target="Lnet/minecraft/core/Registry;getId(Ljava/lang/Object;)I"))
    private static Object injectGetId(Object type) {

        if(type instanceof ServerSideArgumentInfo<?> ss) {
            return ss.getParent();
        }
        return type;
    }

}
