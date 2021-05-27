package me.m1dnightninja.midnightcore.fabric.player;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.math.Vec3d;
import me.m1dnightninja.midnightcore.api.module.skin.ISkinModule;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.ActionBar;
import me.m1dnightninja.midnightcore.api.text.Title;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.common.util.MojangUtil;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.api.Location;
import me.m1dnightninja.midnightcore.fabric.api.PermissionHelper;
import me.m1dnightninja.midnightcore.fabric.util.ConversionUtil;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

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
        if(player == null) return null;

        return MComponent.Serializer.fromJson(Component.Serializer.toJson(player.getName()));
    }

    @Override
    public MComponent getDisplayName() {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return null;

        return MComponent.Serializer.fromJson(Component.Serializer.toJson(player.getDisplayName()));
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
    public void sendMessage(MComponent comp) {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return;

        player.sendMessage(ConversionUtil.toMinecraftComponent(comp), ChatType.SYSTEM, Util.NIL_UUID);
    }

    @Override
    public void sendTitle(Title title) {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return;

        if(title.getOptions().clear) {
            ClientboundSetTitlesPacket packet = new ClientboundSetTitlesPacket(ClientboundSetTitlesPacket.Type.CLEAR, null, 0, 0, 0);
            player.connection.send(packet);
        }

        ClientboundSetTitlesPacket packet = new ClientboundSetTitlesPacket(
                title.getOptions().subtitle ? ClientboundSetTitlesPacket.Type.SUBTITLE : ClientboundSetTitlesPacket.Type.TITLE,
                ConversionUtil.toMinecraftComponent(title.getText()),
                title.getOptions().fadeIn,
                title.getOptions().stay,
                title.getOptions().fadeOut
        );

        player.connection.send(packet);
    }

    @Override
    public void sendActionBar(ActionBar ab) {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return;

        ClientboundSetTitlesPacket packet = new ClientboundSetTitlesPacket(
                ClientboundSetTitlesPacket.Type.ACTIONBAR,
                ConversionUtil.toMinecraftComponent(ab.getText()),
                ab.getOptions().fadeIn,
                ab.getOptions().stay,
                ab.getOptions().fadeOut
        );

        player.connection.send(packet);
    }

    @Override
    public void teleport(MIdentifier dimension, Vec3d location, float yaw, float pitch) {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return;

        Location loc = new Location(ConversionUtil.toResourceLocation(dimension), location.getX(), location.getY(), location.getZ(), yaw, pitch);
        loc.teleport(player);
    }

    @Override
    public void teleport(Vec3d location, float yaw, float pitch) {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return;

        Location loc = new Location(player.level.dimension().location(), location.getX(), location.getY(), location.getZ(), yaw, pitch);
        loc.teleport(player);
    }

    @Override
    public void giveItem(MItemStack item) {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return;

        player.inventory.add(ConversionUtil.toMinecraftStack(item));

    }

    @Override
    public boolean hasPermission(String perm) {

        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        if(player == null) return false;

        return player.hasPermissions(2) || PermissionHelper.check(player.getUUID(), perm);
    }

    @Nullable
    public ServerPlayer getMinecraftPlayer() {
        if(player == null) player = MidnightCore.getServer().getPlayerList().getPlayer(getUUID());
        return player;
    }
}
