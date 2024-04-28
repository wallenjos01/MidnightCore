package org.wallentines.mcore.pluginmsg;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
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

    public static StreamCodec<FriendlyByteBuf, MidnightPayload> codec(CustomPacketPayload.Type<MidnightPayload> type) {
        return new StreamCodec<>() {
            @Override
            public @NotNull MidnightPayload decode(FriendlyByteBuf object) {
                return new MidnightPayload(type, object.readRetainedSlice(object.readableBytes()));
            }

            @Override
            public void encode(FriendlyByteBuf object, MidnightPayload obj) {
                object.writeBytes(obj.buffer);
            }
        };
    }

    public static CustomPacketPayload.Type<MidnightPayload> type(ResourceLocation id) {
        return new Type<>(id);
    }

}
