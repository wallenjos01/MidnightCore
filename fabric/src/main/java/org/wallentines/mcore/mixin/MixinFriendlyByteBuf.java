package org.wallentines.mcore.mixin;

import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.EncoderException;
import net.minecraft.SharedConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.text.ModernSerializer;
import org.wallentines.mcore.text.WrappedComponent;
import org.wallentines.mdcfg.codec.NBTCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.io.IOException;

@Mixin(FriendlyByteBuf.class)
public abstract class MixinFriendlyByteBuf {


    @Inject(method = "writeComponent", at=@At("HEAD"), cancellable = true)
    private void onWriteComponent(Component component, CallbackInfoReturnable<FriendlyByteBuf> cir) {

        // Allow MidnightCore components to be sent directly without conversion
        if(component instanceof WrappedComponent wc) {

            try {
                FriendlyByteBuf buf = (FriendlyByteBuf) (Object) this;
                new NBTCodec(false).encode(
                        ConfigContext.INSTANCE,
                        ModernSerializer.INSTANCE.forContext(new GameVersion(SharedConstants.getCurrentVersion().getId(), SharedConstants.getProtocolVersion())),
                        wc.internal,
                        new ByteBufOutputStream(buf));
            } catch (IOException ex) {
                throw new EncoderException("Unable to encode NBT!", ex);
            }

            cir.setReturnValue((FriendlyByteBuf) (Object) this);
        }
    }

}
