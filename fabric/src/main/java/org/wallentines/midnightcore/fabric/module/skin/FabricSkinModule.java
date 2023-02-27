package org.wallentines.midnightcore.fabric.module.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.phys.Vec3;
import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.common.module.skin.AbstractSkinModule;
import org.wallentines.midnightcore.common.util.MojangUtil;
import org.wallentines.midnightcore.fabric.event.player.PacketSendEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerLeaveEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerLoginEvent;
import org.wallentines.midnightcore.fabric.mixin.AccessorClientboundPlayerInfoUpdatePacket;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.server.FabricServer;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.ModuleInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class FabricSkinModule extends AbstractSkinModule {


    private FabricServer server;

    @Override
    public boolean initialize(ConfigSection config, MServer server) {

        super.initialize(config, server);

        this.server = (FabricServer) server;

        Event.register(PlayerLoginEvent.class, this, event -> {

            MPlayer player = FabricPlayer.wrap(event.getPlayer());
            MinecraftServer mc = this.server.getInternal();

            Skin s = MojangUtil.getSkinFromProfile(event.getProfile());
            setLoginSkin(player, s);
            if(getOfflineModeSkins && !mc.usesAuthentication()) {

                findOfflineModeSkin(player, event.getProfile());

            } else {

                setActiveSkin(player, s);
            }
        });
        Event.register(PlayerLeaveEvent.class, this, event -> {

            MPlayer player = server.getPlayerManager().getPlayer(event.getPlayer().getUUID());
            onLeave(player);
        });

        Event.register(PacketSendEvent.class, this, event -> {

            if(event.getPacket() instanceof ClientboundPlayerInfoUpdatePacket pck) {

                if(pck.entries().isEmpty()) return;

                MPlayer mpl = server.getPlayerManager().getPlayer(pck.entries().get(0).profileId());
                applyActiveProfile(pck, getActiveSkin(mpl), this.server);
            }
        });

        return true;
    }

    @Override
    public void doUpdate(MPlayer uid, Skin skin) {

        ServerPlayer spl = FabricPlayer.getInternal(uid);
        updateSkin(spl);
    }

    private void updateSkin(ServerPlayer player) {

        MinecraftServer server = player.getServer();
        if(server == null) return;

        // Make sure the player is ready to receive a respawn packet
        player.stopRiding();
        Vec3 velocity = player.getDeltaMovement();

        // Create Packets
        ClientboundPlayerInfoRemovePacket remove = new ClientboundPlayerInfoRemovePacket(List.of(player.getUUID()));
        ClientboundPlayerInfoUpdatePacket add = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(player));

        List<Pair<EquipmentSlot, ItemStack>> items = new ArrayList<>();

        items.add(new Pair<>(EquipmentSlot.MAINHAND, player.getMainHandItem()));
        items.add(new Pair<>(EquipmentSlot.OFFHAND,  player.getOffhandItem()));
        items.add(new Pair<>(EquipmentSlot.HEAD,     player.getInventory().armor.get(3)));
        items.add(new Pair<>(EquipmentSlot.CHEST,    player.getInventory().armor.get(2)));
        items.add(new Pair<>(EquipmentSlot.LEGS,     player.getInventory().armor.get(1)));
        items.add(new Pair<>(EquipmentSlot.FEET,     player.getInventory().armor.get(0)));

        ClientboundSetEquipmentPacket equip = new ClientboundSetEquipmentPacket(player.getId(), items);

        ClientboundRemoveEntitiesPacket destroy = new ClientboundRemoveEntitiesPacket(player.getId());
        ClientboundAddPlayerPacket spawn = new ClientboundAddPlayerPacket(player);

        List<SynchedEntityData.DataValue<?>> entityData = player.getEntityData().getNonDefaultValues();
        ClientboundSetEntityDataPacket tracker = null;
        if(entityData != null) {
            tracker = new ClientboundSetEntityDataPacket(player.getId(), entityData);
        }

        float headRot = player.getYHeadRot();
        int rot = (int) headRot;
        if (headRot < (float) rot) rot -= 1;
        ClientboundRotateHeadPacket head = new ClientboundRotateHeadPacket(player, (byte) ((rot * 256.0F) / 360.0F));

        ServerLevel world = player.getLevel();

        ClientboundRespawnPacket respawn = new ClientboundRespawnPacket(
                world.dimensionTypeId(),
                world.dimension(),
                BiomeManager.obfuscateSeed(world.getSeed()),
                player.gameMode.getGameModeForPlayer(),
                player.gameMode.getPreviousGameModeForPlayer(),
                world.isDebug(),
                world.isFlat(),
                (byte) 3, // Preserve Metadata
                Optional.empty()
        );

        ClientboundPlayerPositionPacket position = new ClientboundPlayerPositionPacket(player.getX(), player.getY(), player.getZ(), player.getRotationVector().y, player.getRotationVector().x, new HashSet<>(), 0, false);

        // Send Packets

        for(ServerPlayer obs : server.getPlayerList().getPlayers()) {

            obs.connection.send(remove);
            obs.connection.send(add);

            if(obs == player || !player.getLevel().equals(obs.getLevel())) continue;

            obs.connection.send(destroy);
            obs.connection.send(spawn);
            obs.connection.send(head);
            obs.connection.send(equip);
            if(tracker != null) obs.connection.send(tracker);
        }

        player.connection.send(respawn);
        player.connection.send(position);
        player.connection.send(equip);

        server.getPlayerList().sendPlayerPermissionLevel(player);
        server.getPlayerList().sendAllPlayerInfo(player);

        player.onUpdateAbilities();
        player.getInventory().tick();

        player.setDeltaMovement(velocity);
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
    }

    private static void applyActiveProfile(ClientboundPlayerInfoUpdatePacket packet, Skin skin, FabricServer server) {

        // Will only get entries if ADD_PLAYER is present
        List<ClientboundPlayerInfoUpdatePacket.Entry> entries = packet.newEntries();
        if(entries.isEmpty()) return;

        ClientboundPlayerInfoUpdatePacket.Entry entry = null;
        int index = 0;
        for(; index < entries.size() ; index++) {
            ClientboundPlayerInfoUpdatePacket.Entry ent = entries.get(index);
            for(MPlayer u : server.getPlayerManager()) {
                if(u.getUUID().equals(ent.profileId())) {
                    entry = ent;
                    break;
                }
            }
        }
        if(entry == null) return;
        GameProfile profile = new GameProfile(entry.profileId(), entry.profile().getName());

        ServerPlayer player = server.getInternal().getPlayerList().getPlayer(profile.getId());
        if(player == null) return;

        profile.getProperties().clear();
        profile.getProperties().putAll(player.getGameProfile().getProperties());

        if(skin != null) {
            profile.getProperties().get("textures").clear();
            profile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
        }

        List<ClientboundPlayerInfoUpdatePacket.Entry> newEntries = new ArrayList<>(entries);

        newEntries.set(index - 1, new ClientboundPlayerInfoUpdatePacket.Entry(
            profile.getId(),
            profile,
            entry.listed(),
            entry.latency(),
            entry.gameMode(),
            entry.displayName(),
            entry.chatSession()
        ));

        ((AccessorClientboundPlayerInfoUpdatePacket) packet).setEntries(newEntries);
    }

    public static final ModuleInfo<MServer, ServerModule> MODULE_INFO = new ModuleInfo<>(FabricSkinModule::new, ID, DEFAULT_CONFIG);

}
