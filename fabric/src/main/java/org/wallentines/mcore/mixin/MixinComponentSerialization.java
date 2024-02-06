package org.wallentines.mcore.mixin;

import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.EncoderException;
import net.minecraft.SharedConstants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.text.ModernSerializer;
import org.wallentines.mcore.text.WrappedComponent;
import org.wallentines.mdcfg.codec.NBTCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.io.IOException;

@Mixin(ComponentSerialization.class)
public class MixinComponentSerialization {

    @Shadow @Final @Mutable public static StreamCodec<RegistryFriendlyByteBuf, Component> STREAM_CODEC;

    @Redirect(method="<clinit>", at=@At(value="FIELD", target="Lnet/minecraft/network/chat/ComponentSerialization;STREAM_CODEC:Lnet/minecraft/network/codec/StreamCodec;", opcode = Opcodes.PUTSTATIC))
    private static void redirectComponent(StreamCodec<RegistryFriendlyByteBuf, Component> value) {

        STREAM_CODEC = new StreamCodec<>() {
            @Override
            public @NotNull Component decode(RegistryFriendlyByteBuf buf) {
                return value.decode(buf);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf object, Component cmp) {

                // Encode wrapped components directly
                if (cmp instanceof WrappedComponent wc) {

                    try {
                        new NBTCodec(false).encode(
                                ConfigContext.INSTANCE,
                                ModernSerializer.INSTANCE.forContext(new GameVersion(SharedConstants.getCurrentVersion().getId(), SharedConstants.getProtocolVersion())),
                                wc.internal,
                                new ByteBufOutputStream(object));
                        return;

                    } catch (IOException ex) {
                        throw new EncoderException("Unable to encode NBT!", ex);
                    }
                }

                value.encode(object, cmp);
            }
        };
    }

}
