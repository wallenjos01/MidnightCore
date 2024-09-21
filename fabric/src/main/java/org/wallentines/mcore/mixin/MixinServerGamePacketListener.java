package org.wallentines.mcore.mixin;

import io.netty.buffer.Unpooled;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.mcore.CookieHolder;
import org.wallentines.mcore.CookieResponse;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.midnightlib.event.HandlerList;

@Mixin(ServerCommonPacketListenerImpl.class)
@Implements(@Interface(iface = CookieHolder.class, prefix = "mcore$"))
public class MixinServerGamePacketListener {

    @Unique
    private final HandlerList<CookieResponse> mcore$cookies = new HandlerList<>();

    @Inject(method = "handleCookieResponse", at=@At("HEAD"))
    private void onCookieResponse(ServerboundCookieResponsePacket packet, CallbackInfo ci) {

        ServerCommonPacketListenerImpl self = (ServerCommonPacketListenerImpl) (Object) this;
        if(!(self instanceof ServerGamePacketListenerImpl)) return;

        mcore$cookies.invoke(new CookieResponse(((ServerGamePacketListenerImpl) self).player, ConversionUtil.toIdentifier(packet.key()), Unpooled.wrappedBuffer(packet.payload())));
    }

    public HandlerList<CookieResponse> mcore$responseEvent() {
        return mcore$cookies;
    }

}
