package org.wallentines.mcore.pluginmsg;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.midnightlib.registry.Identifier;

public class MidnightPayload implements CustomPacketPayload {

    private final Type<MidnightPayload> type;
    private final ByteBuf buffer;

    public MidnightPayload(Identifier id, ByteBuf buffer) {
        this.type = type(ConversionUtil.toResourceLocation(id));
        this.buffer = buffer;
    }

    public MidnightPayload(Type<MidnightPayload> type, ByteBuf buffer) {
        this.type = type;
        this.buffer = buffer;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return type;
    }

    public ByteBuf getBuffer() {
        return buffer;
    }

    public static StreamCodec<RegistryFriendlyByteBuf, MidnightPayload> codec(CustomPacketPayload.Type<MidnightPayload> type) {
        return new StreamCodec<>() {
            @Override
            public @NotNull MidnightPayload decode(RegistryFriendlyByteBuf object) {
                return new MidnightPayload(type, object);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf object, MidnightPayload obj) {
                object.writeBytes(obj.buffer);
            }
        };
    }

    public static CustomPacketPayload.Type<MidnightPayload> type(ResourceLocation id) {
        return new Type<>(id);
    }

}
