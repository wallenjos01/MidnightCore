package org.wallentines.midnightcore.fabric.module.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.BiomeManager;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.common.module.skin.AbstractSkinModule;
import org.wallentines.midnightcore.common.util.MojangUtil;
import org.wallentines.midnightcore.fabric.MidnightCore;
import org.wallentines.midnightcore.fabric.event.player.PacketSendEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerLeaveEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerLoginEvent;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.ModuleInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class FabricSkinModule extends AbstractSkinModule {

    @Override
    public boolean initialize(ConfigSection config, MidnightCoreAPI api) {

        super.initialize(config, api);

        Event.register(PlayerLoginEvent.class, this, event -> {

            MPlayer player = FabricPlayer.wrap(event.getPlayer());
            MinecraftServer server = event.getPlayer().getServer();

            Skin s = MojangUtil.getSkinFromProfile(event.getProfile());
            setLoginSkin(player, s);
            if(getOfflineModeSkins && server != null && !server.usesAuthentication()) {

                findOfflineSkin(player, event.getProfile());

            } else {

                setActiveSkin(player, s);
            }
        });
        Event.register(PlayerLeaveEvent.class, this, event -> {

            MPlayer player = MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUUID());
            onLeave(player);
        });

        Event.register(PacketSendEvent.class, this, event -> {

            if(event.getPacket() instanceof ClientboundPlayerInfoPacket pck) {

                MPlayer mpl = MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(pck.getEntries().get(0).getProfile().getId());
                applyActiveProfile(pck, getActiveSkin(mpl));
            }
        });

        return true;
    }

    @Override
    public void doUpdate(MPlayer uid, Skin skin) {

        ServerPlayer spl = FabricPlayer.getInternal(uid);
        updateSkin(spl, skin);
    }

    private void updateSkin(ServerPlayer player, Skin skin) {

        MinecraftServer server = player.getServer();
        if(server == null) return;

        // Create Packets

        ClientboundPlayerInfoPacket remove = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, player);
        ClientboundPlayerInfoPacket add = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, player);

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
        ClientboundSetEntityDataPacket tracker = new ClientboundSetEntityDataPacket(player.getId(), player.getEntityData(), true);

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
                true,
                Optional.empty()
        );

        ClientboundPlayerPositionPacket position = new ClientboundPlayerPositionPacket(player.getX(), player.getY(), player.getZ(), player.getRotationVector().y, player.getRotationVector().x, new HashSet<>(), 0, false);
        ClientboundPlayerAbilitiesPacket abilities = new ClientboundPlayerAbilitiesPacket(player.getAbilities());

        // Send Packets

        for(ServerPlayer obs : server.getPlayerList().getPlayers()) {

            obs.connection.send(remove);
            obs.connection.send(add);

            if(obs == player || !player.getLevel().equals(obs.getLevel())) continue;

            obs.connection.send(destroy);
            obs.connection.send(spawn);
            obs.connection.send(head);
            obs.connection.send(tracker);
            obs.connection.send(equip);

        }

        player.connection.send(respawn);
        player.connection.send(position);
        player.connection.send(abilities);
        player.connection.send(equip);

        server.getPlayerList().sendPlayerPermissionLevel(player);
        server.getPlayerList().sendAllPlayerInfo(player);

        player.getInventory().tick();
    }

    private void applyActiveProfile(ClientboundPlayerInfoPacket packet, Skin skin) {

        if(skin == null) return;

        if(packet.getAction() != ClientboundPlayerInfoPacket.Action.ADD_PLAYER) return;
        List<ClientboundPlayerInfoPacket.PlayerUpdate> entries = packet.getEntries();

        GameProfile profile = null;
        for(ClientboundPlayerInfoPacket.PlayerUpdate ent : entries) {
            for(MPlayer u : MidnightCoreAPI.getInstance().getPlayerManager()) {
                if(u.getUUID().equals(ent.getProfile().getId())) {
                    profile = ent.getProfile();
                    break;
                }
            }
            if(profile != null) break;
        }

        if(profile == null) return;

        GameProfile oldProfile = profile;

        profile = new GameProfile(oldProfile.getId(), oldProfile.getName());

        ServerPlayer player = MidnightCore.getInstance().getServer().getPlayerList().getPlayer(profile.getId());
        if(player == null) return;

        profile.getProperties().clear();
        profile.getProperties().putAll(player.getGameProfile().getProperties());

        profile.getProperties().get("textures").clear();
        profile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));

        ProfilePublicKey.Data keyData = player.getProfilePublicKey() == null ? null : player.getProfilePublicKey().data();

        entries.set(0, new ClientboundPlayerInfoPacket.PlayerUpdate(
                profile,
                player.latency,
                player.gameMode.getGameModeForPlayer(),
                player.getTabListDisplayName(),
                keyData
        ));
    }

    public static final ModuleInfo<MidnightCoreAPI> MODULE_INFO = new ModuleInfo<>(FabricSkinModule::new, ID, DEFAULT_CONFIG);

}
