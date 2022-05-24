package org.wallentines.midnightcore.fabric.player;

import com.mojang.authlib.GameProfile;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.commands.PlaySoundCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.midnightcore.common.player.AbstractPlayer;
import org.wallentines.midnightcore.fabric.MidnightCore;
import org.wallentines.midnightcore.fabric.item.FabricItem;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightcore.fabric.util.LocationUtil;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class FabricPlayer extends AbstractPlayer<ServerPlayer> {

    String locale = "en_us";

    protected FabricPlayer(UUID uuid) {
        super(uuid);
    }

    @Override
    public String getUsername() {
        return run(player -> { return player.getGameProfile().getName(); }, () -> getUUID().toString());
    }

    @Override
    public MComponent getName() {
        return run(player -> ConversionUtil.toMComponent(player.getDisplayName()),
            () -> {
                Optional<GameProfile> prof = MidnightCore.getInstance().getServer().getProfileCache().get(getUUID());
                return prof.map(gameProfile -> new MTextComponent(gameProfile.getName())).orElseGet(() -> new MTextComponent(getUUID().toString()));
            }
        );
    }

    @Override
    public Location getLocation() {
        return run(LocationUtil::getEntityLocation, () -> null);
    }

    @Override
    public MItemStack getItemInMainHand() {
        return run(player -> new FabricItem(player.getItemInHand(InteractionHand.MAIN_HAND)), () -> null);
    }

    @Override
    public MItemStack getItemInOffhand() {
        return run(player -> new FabricItem(player.getItemInHand(InteractionHand.OFF_HAND)), () -> null);
    }

    @Override
    public String getLocale() {
        return locale;
    }

    @Override
    public boolean hasPermission(String permission) {
        return run(player -> Permissions.check(player, permission), () -> false);
    }

    @Override
    public boolean hasPermission(String permission, int permissionLevel) {
        return run(player -> Permissions.check(player, permission, permissionLevel), () -> false);
    }

    @Override
    public void sendMessage(MComponent component) {

        run(player -> player.sendMessage(ConversionUtil.toComponent(component), ChatType.SYSTEM, Util.NIL_UUID), () -> {});
    }

    @Override
    public void sendActionBar(MComponent component) {

        run(player -> {

            player.sendMessage(ConversionUtil.toComponent(component), ChatType.GAME_INFO, Util.NIL_UUID);

        }, () -> { });
    }

    @Override
    public void sendTitle(MComponent component, int fadeIn, int stay, int fadeOut) {

        run(player -> {

            ClientboundSetTitleTextPacket title = new ClientboundSetTitleTextPacket(ConversionUtil.toComponent(component));
            ClientboundSetTitlesAnimationPacket anim = new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut);

            player.connection.send(title);
            player.connection.send(anim);
        }, () -> { });
    }

    @Override
    public void sendSubtitle(MComponent component, int fadeIn, int stay, int fadeOut) {

        run(player -> {

            ClientboundSetSubtitleTextPacket subtitle = new ClientboundSetSubtitleTextPacket(ConversionUtil.toComponent(component));
            ClientboundSetTitlesAnimationPacket anim = new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut);

            player.connection.send(subtitle);
            player.connection.send(anim);
        }, () -> { });
    }

    @Override
    public void clearTitles() {

        run(player -> player.connection.send(new ClientboundClearTitlesPacket(true)), () -> { });
    }

    @Override
    public void playSound(Identifier soundId, String category, float volume, float pitch) {

        run(player -> {

            SoundSource src = SoundSource.valueOf(category.toUpperCase(Locale.ROOT));

            player.connection.send(new ClientboundCustomSoundPacket(
                    ConversionUtil.toResourceLocation(soundId),
                    src, player.position(), volume, pitch
            ));

        }, () -> { });

    }

    @Override
    public void closeContainer() {

        run(ServerPlayer::closeContainer, () -> { });
    }

    @Override
    public void executeCommand(String cmd) {

        run(player -> MidnightCore.getInstance().getServer().getCommands().performCommand(player.createCommandSourceStack(), cmd), () -> { });
    }

    @Override
    public void sendChatMessage(String message) {

        run(player -> player.connection.send(new ServerboundChatPacket(message)), () -> { });
    }

    @Override
    public void giveItem(MItemStack item) {

        run(player -> {
            FabricItem it = (FabricItem) item;
            player.getInventory().add(it.getInternal());
        }, () -> { });
    }

    @Override
    public void giveItem(MItemStack item, int slot) {

        run(player -> {
            FabricItem it = (FabricItem) item;
            player.getInventory().add(slot, it.getInternal());
        }, () -> { });
    }

    @Override
    public void teleport(Location newLoc) {

        run(player -> LocationUtil.teleport(player, newLoc), () -> { });
    }

    public static FabricPlayer wrap(ServerPlayer player) {
        return (FabricPlayer) MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(player.getUUID());
    }

    public static ServerPlayer getInternal(MPlayer player) {
        return ((FabricPlayer) player).getInternal();
    }

}
