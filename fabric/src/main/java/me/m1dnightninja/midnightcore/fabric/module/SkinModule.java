package me.m1dnightninja.midnightcore.fabric.module;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import me.m1dnightninja.midnightcore.api.skin.Skin;
import me.m1dnightninja.midnightcore.common.MojangUtil;
import me.m1dnightninja.midnightcore.common.module.AbstractSkinModule;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.api.event.PacketSendEvent;
import me.m1dnightninja.midnightcore.fabric.api.event.PlayerDisconnectEvent;
import me.m1dnightninja.midnightcore.fabric.api.event.PlayerLoginEvent;
import me.m1dnightninja.midnightcore.fabric.api.event.PlayerSkinUpdateEvent;
import me.m1dnightninja.midnightcore.fabric.event.*;
import me.m1dnightninja.midnightcore.fabric.mixin.AccessorPlayerListPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.BiomeManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class SkinModule extends AbstractSkinModule {


    @Override
    public boolean initialize() {

        Event.register(PlayerLoginEvent.class, this, event -> {
            loginSkins.put(event.getPlayer().getUUID(), MojangUtil.getSkinFromProfile(event.getProfile()));
            activeSkins.put(event.getPlayer().getUUID(), MojangUtil.getSkinFromProfile(event.getProfile()));
        });
        Event.register(PlayerDisconnectEvent.class, this, event -> {
            loginSkins.remove(event.getPlayer().getUUID());
            loadedSkins.remove(event.getPlayer().getUUID());
            activeSkins.remove(event.getPlayer().getUUID());
        });
        Event.register(PacketSendEvent.class, this, event -> {
            if(event.getPacket() instanceof ClientboundPlayerInfoPacket) {
                ClientboundPlayerInfoPacket pck = (ClientboundPlayerInfoPacket) event.getPacket();
                applyActiveProfile(pck);
            }
        });

        return true;
    }

    @Override
    public void updateSkin(UUID uid) {
        if(!loadedSkins.containsKey(uid) || !activeSkins.containsKey(uid)) return;

        ServerPlayer pl = MidnightCore.getServer().getPlayerList().getPlayer(uid);
        if(pl == null) return;

        Skin oldSkin = activeSkins.get(uid);
        Skin newSkin = loadedSkins.get(uid);

        PlayerSkinUpdateEvent ev = new PlayerSkinUpdateEvent(pl, oldSkin, newSkin);
        Event.invoke(ev);

        if(!ev.isCancelled()) {

            activeSkins.put(uid, ev.getNewSkin());
            updateSkin(pl);
        }

    }

    private void updateSkin(ServerPlayer player) {

        MinecraftServer server = player.getServer();
        if(server == null) return;

        // Create Packets

        ClientboundPlayerInfoPacket remove = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, player);
        ClientboundPlayerInfoPacket add = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, player);

        applyActiveProfile(add);

        List<Pair<EquipmentSlot, ItemStack>> items = new ArrayList<>();

        items.add(new Pair<>(EquipmentSlot.MAINHAND, player.getMainHandItem()));
        items.add(new Pair<>(EquipmentSlot.OFFHAND, player.getOffhandItem()));
        items.add(new Pair<>(EquipmentSlot.HEAD, player.inventory.armor.get(3)));
        items.add(new Pair<>(EquipmentSlot.CHEST, player.inventory.armor.get(2)));
        items.add(new Pair<>(EquipmentSlot.LEGS, player.inventory.armor.get(1)));
        items.add(new Pair<>(EquipmentSlot.FEET, player.inventory.armor.get(0)));

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
                world.dimensionType(),
                world.dimension(),
                BiomeManager.obfuscateSeed(world.getSeed()),
                player.gameMode.getGameModeForPlayer(),
                player.gameMode.getPreviousGameModeForPlayer(),
                world.isDebug(),
                world.isFlat(),
                true
        );

        ClientboundPlayerPositionPacket position = new ClientboundPlayerPositionPacket(player.getX(), player.getY(), player.getZ(), player.xRot, player.yRot, new HashSet<>(), 0);
        ClientboundPlayerAbilitiesPacket abilities = new ClientboundPlayerAbilitiesPacket(player.abilities);

        // Send Packets

        for(ServerPlayer obs : server.getPlayerList().getPlayers()) {

            obs.connection.send(remove);
            obs.connection.send(add);

            obs.connection.send(equip);

            if(obs == player || !player.getLevel().equals(obs.getLevel())) continue;

            obs.connection.send(destroy);
            obs.connection.send(spawn);
            obs.connection.send(head);
            obs.connection.send(tracker);

        }

        player.connection.send(respawn);
        player.connection.send(position);
        player.connection.send(abilities);

        server.getPlayerList().sendPlayerPermissionLevel(player);
        server.getPlayerList().sendAllPlayerInfo(player);

        player.inventory.tick();
    }

    private void applyActiveProfile(ClientboundPlayerInfoPacket packet) {

        if(((AccessorPlayerListPacket) packet).getAction() != ClientboundPlayerInfoPacket.Action.ADD_PLAYER) return;
        List<ClientboundPlayerInfoPacket.PlayerUpdate> entries = ((AccessorPlayerListPacket) packet).getEntries();

        GameProfile profile = null;
        for(ClientboundPlayerInfoPacket.PlayerUpdate ent : entries) {
            for(UUID u : loginSkins.keySet()) {
                if(u.equals(ent.getProfile().getId())) {
                    profile = ent.getProfile();
                    break;
                }
            }
            if(profile != null) break;
        }

        if(profile == null) return;

        Skin skin = activeSkins.get(profile.getId());

        if(skin == null) return;

        GameProfile oldProfile = profile;

        profile = new GameProfile(oldProfile.getId(), oldProfile.getName());
        profile.getProperties().putAll(oldProfile.getProperties());

        ServerPlayer player = MidnightCore.getServer().getPlayerList().getPlayer(profile.getId());
        if(player == null) return;

        profile.getProperties().clear();
        profile.getProperties().putAll(player.getGameProfile().getProperties());

        profile.getProperties().get("textures").clear();
        profile.getProperties().put("textures", new Property("textures", skin.getBase64(), skin.getSignature()));

        entries.set(0, packet.new PlayerUpdate(
                profile,
                player.latency,
                player.gameMode.getGameModeForPlayer(),
                player.getTabListDisplayName()
        ));
    }
}
