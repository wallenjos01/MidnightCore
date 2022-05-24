package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.module.skin.SkinModule;
import org.wallentines.midnightcore.api.module.skin.Skinnable;
import org.wallentines.midnightcore.fabric.event.player.ContainerCloseEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerChangeDimensionEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerDropItemEvent;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightlib.event.Event;

@Mixin(ServerPlayer.class)
public class MixinServerPlayer implements Skinnable {

    private final SkinModule midnight_core_skinModule = MidnightCoreAPI.getInstance().getModuleManager().getModule(SkinModule.class);

    @Inject(method = "doCloseContainer()V", at = @At("HEAD"))
    private void onClose(CallbackInfo ci) {

        ServerPlayer pl = (ServerPlayer) (Object) this;

        ContainerCloseEvent ev = new ContainerCloseEvent(pl, pl.containerMenu);
        Event.invoke(ev);
    }

    @Inject(method = "drop(Z)Z", at=@At(value = "INVOKE", target="Lnet/minecraft/world/entity/player/Inventory;removeFromSelected(Z)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private void onDrop(CallbackInfoReturnable<Boolean> cir) {

        ServerPlayer pl = (ServerPlayer) (Object) this;

        ItemStack selected = pl.getInventory().getSelected();
        PlayerDropItemEvent event = new PlayerDropItemEvent(pl, selected);
        Event.invoke(event);

        if(event.isCancelled()) {
            cir.setReturnValue(false);
            cir.cancel();
            pl.connection.send(new ClientboundContainerSetSlotPacket(0, pl.inventoryMenu.incrementStateId(), pl.getInventory().selected + 36, selected));
        }
    }

    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V", at=@At(value="INVOKE", target="Lnet/minecraft/server/level/ServerPlayer;getLevel()Lnet/minecraft/server/level/ServerLevel;"), cancellable = true)
    private void onTeleport(ServerLevel serverLevel, double d, double e, double f, float g, float h, CallbackInfo ci) {

        ServerPlayer pl = (ServerPlayer) (Object) this;

        PlayerChangeDimensionEvent event = new PlayerChangeDimensionEvent(pl, pl.getLevel(), serverLevel);
        Event.invoke(event);

        if(event.isCancelled()) {
            ci.cancel();
        }
    }

    @Override
    public void setSkin(Skin skin) {
        FabricPlayer pl = FabricPlayer.wrap((ServerPlayer) (Object) this);
        midnight_core_skinModule.setSkin(pl, skin);
        midnight_core_skinModule.updateSkin(pl);
    }

    @Override
    public void resetSkin() {
        FabricPlayer pl = FabricPlayer.wrap((ServerPlayer) (Object) this);
        midnight_core_skinModule.resetSkin(pl);
        midnight_core_skinModule.updateSkin(pl);
    }

    @Override
    public Skin getSkin() {
        FabricPlayer pl = FabricPlayer.wrap((ServerPlayer) (Object) this);
        return midnight_core_skinModule.getSkin(pl);
    }
}
