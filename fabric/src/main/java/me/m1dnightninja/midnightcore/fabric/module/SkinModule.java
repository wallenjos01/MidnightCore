package me.m1dnightninja.midnightcore.fabric.module;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.common.util.MojangUtil;
import me.m1dnightninja.midnightcore.common.module.AbstractSkinModule;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.api.event.*;
import me.m1dnightninja.midnightcore.fabric.event.*;
import me.m1dnightninja.midnightcore.fabric.mixin.AccessorPlayerListPacket;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
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

public class SkinModule extends AbstractSkinModule {


    @Override
    public boolean initialize(ConfigSection config) {

        Event.register(PlayerLoginEvent.class, this, event -> {

            MPlayer player = FabricPlayer.wrap(event.getPlayer());

            if(MidnightCore.getServer().usesAuthentication()) {

                Skin s = MojangUtil.getSkinFromProfile(event.getProfile());
                loginSkins.put(player, s);
                activeSkins.put(player, s);

            } else {

                getOnlineSkinAsync(player.getUUID(), (pl, skin) -> {

                    loginSkins.put(player, skin);

                    if(!activeSkins.containsKey(player)) {

                        activeSkins.put(player, skin);
                        updateSkin(player);
                    }
                });

            }

        });
        Event.register(PlayerDisconnectEvent.class, this, event -> {

            MPlayer player = MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUUID());

            loginSkins.remove(player);
            loadedSkins.remove(player);
            activeSkins.remove(player);
        });
        Event.register(PacketSendEvent.class, this, event -> {
            if(event.getPacket() instanceof ClientboundPlayerInfoPacket) {
                applyActiveProfile((ClientboundPlayerInfoPacket) event.getPacket());
            }
        });

        Event.register(SavePointCreatedEvent.class, this, event -> event.getSavePoint().extraData.set("skin", getSkin(FabricPlayer.wrap(event.getPlayer()))));
        Event.register(SavePointLoadEvent.class, this, event -> setSkin(FabricPlayer.wrap(event.getPlayer()), event.getSavePoint().extraData.get("skin", Skin.class)));

        return true;
    }

    @Override
    public ConfigSection getDefaultConfig() {
        return null;
    }

    @Override
    public void updateSkin(MPlayer uid) {
        if(!loadedSkins.containsKey(uid) || !activeSkins.containsKey(uid)) return;

        ServerPlayer pl = ((FabricPlayer) uid).getMinecraftPlayer();
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
        items.add(new Pair<>(EquipmentSlot.HEAD, player.getInventory().armor.get(3)));
        items.add(new Pair<>(EquipmentSlot.CHEST, player.getInventory().armor.get(2)));
        items.add(new Pair<>(EquipmentSlot.LEGS, player.getInventory().armor.get(1)));
        items.add(new Pair<>(EquipmentSlot.FEET, player.getInventory().armor.get(0)));

        ClientboundSetEquipmentPacket equip = new ClientboundSetEquipmentPacket(player.getId(), items);

        ClientboundRemoveEntityPacket destroy = new ClientboundRemoveEntityPacket(player.getId());
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

        ClientboundPlayerPositionPacket position = new ClientboundPlayerPositionPacket(player.getX(), player.getY(), player.getZ(), player.getRotationVector().y, player.getRotationVector().x, new HashSet<>(), 0, false);
        ClientboundPlayerAbilitiesPacket abilities = new ClientboundPlayerAbilitiesPacket(player.getAbilities());

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

        player.getInventory().tick();
    }

    private void applyActiveProfile(ClientboundPlayerInfoPacket packet) {

        if(((AccessorPlayerListPacket) packet).getAction() != ClientboundPlayerInfoPacket.Action.ADD_PLAYER) return;
        List<ClientboundPlayerInfoPacket.PlayerUpdate> entries = ((AccessorPlayerListPacket) packet).getEntries();

        GameProfile profile = null;
        for(ClientboundPlayerInfoPacket.PlayerUpdate ent : entries) {
            for(MPlayer u : loginSkins.keySet()) {
                if(u.getUUID().equals(ent.getProfile().getId())) {
                    profile = ent.getProfile();
                    break;
                }
            }
            if(profile != null) break;
        }

        if(profile == null) return;

        Skin skin = activeSkins.get(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(profile.getId()));

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

        entries.set(0, new ClientboundPlayerInfoPacket.PlayerUpdate(
                profile,
                player.latency,
                player.gameMode.getGameModeForPlayer(),
                player.getTabListDisplayName()
        ));
    }
}
