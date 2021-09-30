package me.m1dnightninja.midnightcore.fabric.player;

import com.mojang.authlib.GameProfile;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.math.Vec3d;
import me.m1dnightninja.midnightcore.api.module.skin.ISkinModule;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.Location;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.MActionBar;
import me.m1dnightninja.midnightcore.api.text.MTitle;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.common.util.MojangUtil;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.util.LocationUtil;
import me.m1dnightninja.midnightcore.fabric.util.PermissionUtil;
import me.m1dnightninja.midnightcore.fabric.inventory.FabricItem;
import me.m1dnightninja.midnightcore.fabric.util.ConversionUtil;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class FabricPlayer extends MPlayer {

    private ServerPlayer player;

    protected FabricPlayer(UUID u) {
        super(u);
        this.player = MidnightCore.getServer().getPlayerList().getPlayer(u);
    }

    public static MPlayer wrap(ServerPlayer pl) {
        return MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(pl.getUUID());
    }

    @Override
    public MComponent getName() {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) {
            Optional<GameProfile> prof = MidnightCore.getServer().getProfileCache().get(getUUID());
            return prof.map(gameProfile -> MComponent.createTextComponent(gameProfile.getName())).orElseGet(() -> MComponent.createTextComponent(getUUID().toString()));
        }

        return ConversionUtil.fromMinecraftComponent(player.getName());
    }

    @Override
    public MComponent getDisplayName() {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return null;

        return ConversionUtil.fromMinecraftComponent(player.getDisplayName());
    }

    @Override
    public MIdentifier getDimension() {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return null;

        ResourceLocation dim = player.level.dimension().location();

        return MIdentifier.create(dim.getNamespace(), dim.getPath());
    }

    @Override
    public Skin getSkin() {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return null;

        ISkinModule mod = MidnightCoreAPI.getInstance().getModule(ISkinModule.class);
        if(mod == null) {
            return MojangUtil.getSkinFromProfile(player.getGameProfile());
        }

        return mod.getSkin(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(player.getUUID()));
    }

    @Override
    public Vec3d getLocation() {
        return new Vec3d(player.getX(), player.getY(), player.getZ());
    }

    @Override
    public String getServer() {
        return "";
    }

    @Override
    public MItemStack getItemInMainHand() {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return null;

        return new FabricItem(player.getMainHandItem());
    }

    @Override
    public MItemStack getItemInOffHand() {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return null;

        return new FabricItem(player.getOffhandItem());
    }

    @Override
    public float getYaw() {
        return player.getRotationVector().x;
    }

    @Override
    public float getPitch() {
        return player.getRotationVector().y;
    }

    @Override
    public boolean isOffline() {
        return player == null;
    }

    @Override
    public void sendMessage(MComponent comp) {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return;

        player.sendMessage(ConversionUtil.toMinecraftComponent(comp), ChatType.SYSTEM, Util.NIL_UUID);
    }

    @Override
    public void sendTitle(MTitle title) {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return;

        if(title.getOptions().clear) {
            player.connection.send(new ClientboundClearTitlesPacket(true));
        }

        if(title.getOptions().subtitle) {

            player.connection.send(new ClientboundSetSubtitleTextPacket(ConversionUtil.toMinecraftComponent(title.getText())));
        } else {

            player.connection.send(new ClientboundSetTitleTextPacket(ConversionUtil.toMinecraftComponent(title.getText())));
        }

        ClientboundSetTitlesAnimationPacket packet = new ClientboundSetTitlesAnimationPacket(
                title.getOptions().fadeIn,
                title.getOptions().stay,
                title.getOptions().fadeOut
        );

        player.connection.send(packet);
    }

    @Override
    public void sendActionBar(MActionBar ab) {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return;

        player.connection.send(new ClientboundSetActionBarTextPacket(ConversionUtil.toMinecraftComponent(ab.getText())));
    }

    @Override
    public void executeCommand(String cmd) {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return;

        MidnightCore.getServer().getCommands().performCommand(player.createCommandSourceStack(), cmd);
    }

    @Override
    public void sendChatMessage(String message) {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return;

        ServerboundChatPacket packet = new ServerboundChatPacket(message);
        player.connection.handleChat(packet);
    }

    @Override
    public void teleport(MIdentifier dimension, Vec3d location, float yaw, float pitch) {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return;

        Location loc = new Location(dimension, location.getX(), location.getY(), location.getZ(), yaw, pitch);
        LocationUtil.teleport(player, loc);
    }

    @Override
    public void teleport(Vec3d location, float yaw, float pitch) {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return;

        Location loc = new Location(ConversionUtil.fromResourceLocation(player.level.dimension().location()), location.getX(), location.getY(), location.getZ(), yaw, pitch);
        LocationUtil.teleport(player, loc);
    }

    @Override
    public void teleport(Location location) {

        LocationUtil.teleport(player, location);
    }

    @Override
    public void giveItem(MItemStack item) {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return;

        player.getInventory().add(ConversionUtil.toMinecraftStack(item));

    }

    @Override
    protected void cleanup() {
        player = null;
    }

    @Override
    public boolean hasPermission(String perm) {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return false;

        return player.hasPermissions(2) || PermissionUtil.check(player.getUUID(), perm);
    }

    @Nullable
    public ServerPlayer getMinecraftPlayer() {
        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        return player;
    }
}
