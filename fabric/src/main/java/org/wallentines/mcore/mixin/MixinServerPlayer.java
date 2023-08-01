package org.wallentines.mcore.mixin;

import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.mcore.GameMode;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.item.ItemStack;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ContentConverter;
import org.wallentines.mcore.text.WrappedComponent;
import org.wallentines.mcore.util.AuthUtil;


@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer implements Player {

    @Unique
    private String midnightcore$language = "en_us";

    @Shadow public abstract void sendSystemMessage(net.minecraft.network.chat.Component component, boolean bl);
    @Shadow @Final public MinecraftServer server;

    @Shadow @Final public ServerPlayerGameMode gameMode;

    @Shadow public abstract boolean hasDisconnected();

    @Shadow public abstract boolean setGameMode(GameType gameType);

    @Shadow public ServerGamePacketListenerImpl connection;

    @Unique
    @Override
    public String getUsername() {
        return ((net.minecraft.world.entity.player.Player) (Object) this).getGameProfile().getName();
    }

    @Unique
    @Override
    public Server getServer() {
        return server;
    }

    @Unique
    @Override
    public Skin getSkin() {
        return AuthUtil.getProfileSkin(((net.minecraft.world.entity.player.Player) (Object) this).getGameProfile());
    }

    @Unique
    @Override
    public Component getDisplayName() {
        ServerPlayer spl = (ServerPlayer) (Object) this;
        return ContentConverter.convertReverse(spl.getDisplayName());
    }

    @Unique
    @Override
    public boolean isOnline() {
        return !hasDisconnected();
    }

    @Unique
    @Override
    public void sendMessage(Component component) {

        sendSystemMessage(WrappedComponent.resolved(component, this), false);
    }

    @Unique
    @Override
    public void sendActionBar(Component component) {

        sendSystemMessage(WrappedComponent.resolved(component, this), true);
    }

    @Unique
    @Override
    public void sendTitle(Component title) {
        connection.send(new ClientboundSetTitleTextPacket(WrappedComponent.resolved(title, this)));
    }

    @Unique
    @Override
    public void sendSubtitle(Component title) {
        connection.send(new ClientboundSetSubtitleTextPacket(WrappedComponent.resolved(title, this)));
    }

    @Unique
    @Override
    public void clearTitles() {
        connection.send(new ClientboundClearTitlesPacket(false));
    }

    @Unique
    @Override
    public void setTitleTimes(int fadeIn, int stay, int fadeOut) {
        connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
    }

    @Unique
    @Override
    public void resetTitles() {
        connection.send(new ClientboundClearTitlesPacket(true));
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

    @Unique
    @Override
    public GameMode getGameMode() {
        return switch (gameMode.getGameModeForPlayer()) {
            case SURVIVAL -> GameMode.SURVIVAL;
            case CREATIVE -> GameMode.CREATIVE;
            case ADVENTURE -> GameMode.ADVENTURE;
            case SPECTATOR -> GameMode.SPECTATOR;
        };
    }

    @Unique
    @Override
    public void setGameMode(GameMode mode) {
        setGameMode(switch (mode) {
            case SURVIVAL -> GameType.SURVIVAL;
            case CREATIVE -> GameType.CREATIVE;
            case ADVENTURE -> GameType.ADVENTURE;
            case SPECTATOR -> GameType.SPECTATOR;
        });
    }

    @Inject(method="updateOptions", at=@At("RETURN"))
    private void onUpdateOptions(ServerboundClientInformationPacket packet, CallbackInfo ci) {
        midnightcore$language = packet.language();
    }

}
