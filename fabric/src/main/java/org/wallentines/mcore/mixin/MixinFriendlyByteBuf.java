package org.wallentines.mcore.mixin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.wallentines.mcore.text.WrappedComponent;

@Mixin(FriendlyByteBuf.class)
public class MixinFriendlyByteBuf {

    @Redirect(method = "writeComponent", at=@At(value = "INVOKE", target="Lnet/minecraft/network/chat/Component$Serializer;toJson(Lnet/minecraft/network/chat/Component;)Ljava/lang/String;"))
    private String onWriteComponent(Component comp) {

        // Allow MidnightCore components to be sent directly without conversion
        if(comp instanceof WrappedComponent) {
            return ((WrappedComponent) comp).internal.toJSONString();
        }

        return Component.Serializer.toJson(comp);
    }

}
