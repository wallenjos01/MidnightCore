package org.wallentines.mcore.mixin;

import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.mcore.item.ItemStack;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ContentConverter;
import org.wallentines.mcore.text.ComponentResolver;
import org.wallentines.mcore.text.WrappedComponent;


@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer implements Player {

    @Unique
    private String midnightcore$language = "en_us";

    @Shadow public abstract void sendSystemMessage(net.minecraft.network.chat.Component component, boolean bl);


    @Unique
    @Override
    public String getUsername() {
        return ((net.minecraft.world.entity.player.Player) (Object) this).getGameProfile().getName();
    }

    @Unique
    @Override
    public Component getDisplayName() {

        ServerPlayer spl = (ServerPlayer) (Object) this;
        return ContentConverter.convertReverse(spl.getDisplayName());
    }

    @Override
    public void sendMessage(Component component) {

        sendSystemMessage(new WrappedComponent(ComponentResolver.resolveComponent(component, this)), false);
    }

    @Override
    public void sendActionBar(Component component) {

        sendSystemMessage(new WrappedComponent(ComponentResolver.resolveComponent(component, this)), true);
    }

    @Unique
    @Override
    public ItemStack getHandItem() {
        return (ItemStack) (Object) ((net.minecraft.world.entity.player.Player) (Object) this).getItemInHand(InteractionHand.MAIN_HAND);
    }

    @Unique
    @Override
    public ItemStack getOffhandItem() {
        return (ItemStack) (Object) ((net.minecraft.world.entity.player.Player) (Object) this).getItemInHand(InteractionHand.OFF_HAND);
    }

    @Unique
    @Override
    public void giveItem(ItemStack item) {

        if(!((Object) item instanceof net.minecraft.world.item.ItemStack)) {
            throw new IllegalStateException("Attempt to add non-item to player inventory!");
        }

        ((net.minecraft.world.entity.player.Player) (Object) this).getInventory().add((net.minecraft.world.item.ItemStack) (Object) item);
    }

    @Unique
    @Override
    public String getLanguage() {
        return midnightcore$language;
    }

    @Inject(method="updateOptions", at=@At("RETURN"))
    private void onUpdateOptions(ServerboundClientInformationPacket packet, CallbackInfo ci) {
        midnightcore$language = packet.language();
    }

}
