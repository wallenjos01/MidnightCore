package org.wallentines.mcore.skin;

import com.mojang.authlib.GameProfile;
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
import org.wallentines.mcore.*;
import org.wallentines.mcore.event.PlayerJoinEvent;
import org.wallentines.mcore.mixin.AccessorPlayer;
import org.wallentines.mcore.util.AuthUtil;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mcore.util.MojangUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.ModuleInfo;

import java.util.*;

public class FabricSkinModule extends SkinModule {

    private final HashMap<UUID, GameProfile> loginProfiles = new HashMap<>();

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<>(FabricSkinModule::new, ID, DEFAULT_CONFIG);

    @Override
    public void setSkin(Player player, Skin skin) {

        updateProfile(player, AuthUtil.forPlayer(ConversionUtil.validate(player), skin));
    }

    @Override
    public void resetSkin(Player player) {

        updateProfile(player, loginProfiles.get(player.getUUID()));
    }

    @Override
    public boolean initialize(ConfigSection section, Server data) {

        super.initialize(section, data);

        boolean offlineModeSkins = section.getBoolean("get_skins_in_offline_mode");
        Event.register(PlayerJoinEvent.class, this, 10, ev -> {

            loginProfiles.put(ev.getPlayer().getUUID(), ev.getPlayer().getGameProfile());
            if(offlineModeSkins) {
                MojangUtil.getSkinByNameAsync(ev.getPlayer().getGameProfile().getName()).thenAccept(skin -> {
                    GameProfile newProfile = AuthUtil.forPlayer(ev.getPlayer(), skin);
                    loginProfiles.put(ev.getPlayer().getUUID(), newProfile);
                    updateProfile(ev.getPlayer(), newProfile);
                });
            }
        });

        return true;
    }

    private void updateProfile(Player player, GameProfile gameProfile) {

        ServerPlayer spl = ConversionUtil.validate(player);
        ((AccessorPlayer) spl).setGameProfile(gameProfile);

        // Update
        MinecraftServer server = spl.getServer();
        if(server == null) return;

        // Make sure the player is ready to receive a respawn packet
        spl.stopRiding();
        Vec3 velocity = spl.getDeltaMovement();

        // Create Packets
        ClientboundPlayerInfoRemovePacket remove = new ClientboundPlayerInfoRemovePacket(List.of(spl.getUUID()));
        ClientboundPlayerInfoUpdatePacket add = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(spl));

        List<Pair<EquipmentSlot, ItemStack>> items = new ArrayList<>();

        items.add(new Pair<>(EquipmentSlot.MAINHAND, spl.getMainHandItem()));
        items.add(new Pair<>(EquipmentSlot.OFFHAND,  spl.getOffhandItem()));
        items.add(new Pair<>(EquipmentSlot.HEAD,     spl.getInventory().armor.get(3)));
        items.add(new Pair<>(EquipmentSlot.CHEST,    spl.getInventory().armor.get(2)));
        items.add(new Pair<>(EquipmentSlot.LEGS,     spl.getInventory().armor.get(1)));
        items.add(new Pair<>(EquipmentSlot.FEET,     spl.getInventory().armor.get(0)));

        ClientboundSetEquipmentPacket equip = new ClientboundSetEquipmentPacket(spl.getId(), items);

        ServerLevel world = spl.serverLevel();

        ClientboundRespawnPacket respawn = new ClientboundRespawnPacket(
                world.dimensionTypeId(),
                world.dimension(),
                BiomeManager.obfuscateSeed(world.getSeed()),
                spl.gameMode.getGameModeForPlayer(),
                spl.gameMode.getPreviousGameModeForPlayer(),
                world.isDebug(),
                world.isFlat(),
                (byte) 3, // Preserve Metadata
                Optional.empty(),
                0
        );

        ClientboundPlayerPositionPacket position = new ClientboundPlayerPositionPacket(spl.getX(), spl.getY(), spl.getZ(), spl.getRotationVector().y, spl.getRotationVector().x, new HashSet<>(), 0);

        // Player information packets should be sent to everyone
        for(ServerPlayer obs : server.getPlayerList().getPlayers()) {

            obs.connection.send(remove);
            obs.connection.send(add);
        }

        // Entity information packets should only be sent to observers in the same world
        Collection<ServerPlayer> observers = world.getPlayers(pl -> pl != spl);
        if(observers.size() > 0) {

            ClientboundRemoveEntitiesPacket destroy = new ClientboundRemoveEntitiesPacket(spl.getId());
            ClientboundAddPlayerPacket spawn = new ClientboundAddPlayerPacket(spl);

            List<SynchedEntityData.DataValue<?>> entityData = spl.getEntityData().getNonDefaultValues();
            ClientboundSetEntityDataPacket tracker = null;
            if (entityData != null) {
                tracker = new ClientboundSetEntityDataPacket(spl.getId(), entityData);
            }

            float headRot = spl.getYHeadRot();
            int rot = (int) headRot;
            if (headRot < (float) rot) rot -= 1;
            ClientboundRotateHeadPacket head = new ClientboundRotateHeadPacket(spl, (byte) ((rot * 256.0F) / 360.0F));

            for (ServerPlayer obs : observers) {

                obs.connection.send(destroy);
                obs.connection.send(spawn);
                obs.connection.send(head);
                obs.connection.send(equip);
                if(tracker != null) obs.connection.send(tracker);
            }
        }

        // The remaining packets should only be sent to the player themselves
        spl.connection.send(respawn);
        spl.connection.send(position);
        spl.connection.send(equip);

        server.getPlayerList().sendPlayerPermissionLevel(spl);
        server.getPlayerList().sendAllPlayerInfo(spl);

        spl.onUpdateAbilities();
        spl.getInventory().tick();

        spl.setDeltaMovement(velocity);
        spl.connection.send(new ClientboundSetEntityMotionPacket(spl));

    }
}
