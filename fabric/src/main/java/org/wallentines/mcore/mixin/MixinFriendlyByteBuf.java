package org.wallentines.mcore.mixin;

import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.wallentines.mcore.text.ModernSerializer;
import org.wallentines.mcore.text.WrappedComponent;
import org.wallentines.mcore.util.NBTContext;

@Mixin(FriendlyByteBuf.class)
public abstract class MixinFriendlyByteBuf {

    @Shadow public abstract FriendlyByteBuf writeNbt(@Nullable Tag tag);

    @Inject(method = "writeComponent", at=@At("HEAD"), cancellable = true)
    private void onWriteComponent(Component component, CallbackInfoReturnable<FriendlyByteBuf> cir) {

        // Allow MidnightCore components to be sent directly without conversion
        if(component instanceof WrappedComponent) {
            Tag out = ModernSerializer.INSTANCE.serialize(NBTContext.INSTANCE, ((WrappedComponent) component).internal).getOrThrow();
            writeNbt(out);
            cir.setReturnValue((FriendlyByteBuf) (Object) this);
        }
    }

}
