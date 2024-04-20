package org.wallentines.mcore.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.wallentines.mcore.ServerSideArgumentInfo;

@Mixin(targets = "net.minecraft.network.protocol.game.ClientboundCommandsPacket$ArgumentNodeStub")
public class MixinClientboundCommandsPacket {

    @ModifyArg(
            method="serializeCap(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/commands/synchronization/ArgumentTypeInfo;Lnet/minecraft/commands/synchronization/ArgumentTypeInfo$Template;)V",
            at=@At(value="INVOKE", target="Lnet/minecraft/core/Registry;getId(Ljava/lang/Object;)I"))
    private static Object injectGetId(Object obj) {

        if(obj instanceof ServerSideArgumentInfo<?> ss) {
            return ss.getParent();
        }
        return obj;
    }

}
