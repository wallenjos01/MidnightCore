package org.wallentines.midnightcore.spigot.adapter;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.EmptyByteBuf;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.player.MPlayer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SkinUpdater_v1_12_R1 implements SkinUpdater {

    public static final SkinUpdater INSTANCE = new SkinUpdater_v1_12_R1();
    private Field action;
    private Field entries;

    @Override
    public void init() {

        try {
            action = PacketPlayOutPlayerInfo.class.getDeclaredField("a");
            entries = PacketPlayOutPlayerInfo.class.getDeclaredField("b");
        } catch (NoSuchFieldException ex) {
            throw new IllegalStateException("Unable to find required field in PacketPlayOutPlayerInfo!");
        }
        action.setAccessible(true);
        entries.setAccessible(true);
    }

    @Override
    public void updateSkin(Player spl, Skin skin) {

        EntityPlayer epl = ((CraftPlayer) spl).getHandle();

        MinecraftServer server = epl.server;
        if(server == null) return;

        // Create Packets

        PacketPlayOutPlayerInfo remove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, epl);
        PacketPlayOutPlayerInfo add = createPacket(epl, skin);

        PacketPlayOutEntityEquipment equipM = new PacketPlayOutEntityEquipment(epl.getId(), EnumItemSlot.MAINHAND, epl.getItemInMainHand());
        PacketPlayOutEntityEquipment equipO = new PacketPlayOutEntityEquipment(epl.getId(), EnumItemSlot.OFFHAND, epl.getItemInOffHand());
        PacketPlayOutEntityEquipment equipH = new PacketPlayOutEntityEquipment(epl.getId(), EnumItemSlot.HEAD, epl.getEquipment(EnumItemSlot.HEAD));
        PacketPlayOutEntityEquipment equipC = new PacketPlayOutEntityEquipment(epl.getId(), EnumItemSlot.CHEST, epl.getEquipment(EnumItemSlot.CHEST));
        PacketPlayOutEntityEquipment equipL = new PacketPlayOutEntityEquipment(epl.getId(), EnumItemSlot.LEGS, epl.getEquipment(EnumItemSlot.LEGS));
        PacketPlayOutEntityEquipment equipF = new PacketPlayOutEntityEquipment(epl.getId(), EnumItemSlot.FEET, epl.getEquipment(EnumItemSlot.FEET));

        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(epl.getId());
        PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(epl);
        PacketPlayOutEntityMetadata tracker = new PacketPlayOutEntityMetadata(epl.getId(), epl.getDataWatcher(), true);

        float headRot = spl.getEyeLocation().getYaw();
        int rot = (int) headRot;
        if (headRot < (float) rot) rot -= 1;
        PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation(epl, (byte) ((rot * 256.0F) / 360.0F));

        WorldServer world = (WorldServer) epl.world;

        PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(world.dimension, world.getDifficulty(), world.getWorldData().getType(), epl.playerInteractManager.getGameMode());

        PacketPlayOutPosition position = new PacketPlayOutPosition(spl.getLocation().getX(), spl.getLocation().getY(), spl.getLocation().getZ(), spl.getLocation().getYaw(), spl.getLocation().getPitch(), new HashSet<>(), 0);
        PacketPlayOutAbilities abilities = new PacketPlayOutAbilities(epl.abilities);

        // Send Packets
        for(EntityPlayer obs : server.getPlayerList().players) {

            obs.playerConnection.sendPacket(remove);
            obs.playerConnection.sendPacket(add);

            if(!epl.world.equals(obs.world)) continue;

            obs.playerConnection.sendPacket(equipM);
            obs.playerConnection.sendPacket(equipO);
            obs.playerConnection.sendPacket(equipH);
            obs.playerConnection.sendPacket(equipC);
            obs.playerConnection.sendPacket(equipL);
            obs.playerConnection.sendPacket(equipF);

            if(obs == epl) continue;

            obs.playerConnection.sendPacket(destroy);
            obs.playerConnection.sendPacket(spawn);
            obs.playerConnection.sendPacket(head);
            obs.playerConnection.sendPacket(tracker);

        }

        epl.playerConnection.sendPacket(respawn);
        epl.playerConnection.sendPacket(position);
        epl.playerConnection.sendPacket(abilities);

        server.getPlayerList().updateClient(epl);

        spl.updateInventory();
    }

    private PacketPlayOutPlayerInfo createPacket(EntityPlayer player, Skin skin) {

        PacketDataSerializer serializer = new PacketDataSerializer(new EmptyByteBuf(ByteBufAllocator.DEFAULT));

        serializer.a(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);
        serializer.d(1);
        serializer.a(player.getProfile().getId());
        serializer.a(player.getProfile().getName());
        serializer.d(1);
        serializer.a("textures");
        serializer.a(skin.getValue());
        serializer.writeBoolean(true);
        serializer.a(skin.getSignature());
        serializer.a(player.playerInteractManager.getGameMode());
        serializer.d(player.ping);
        serializer.writeBoolean(false);

        PacketPlayOutPlayerInfo out = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, new ArrayList<>());
        try {
            out.a(serializer);
        } catch (IOException ex) {
            // Ignore
        }

        return out;
    }

 /*   @SuppressWarnings("unchecked")
    private void applyActiveProfile(PacketPlayOutPlayerInfo packet, Skin skin) {

        PacketPlayOutPlayerInfo.EnumPlayerInfoAction act;
        List<PacketPlayOutPlayerInfo.PlayerInfoData> ents;
        try {
            act = (PacketPlayOutPlayerInfo.EnumPlayerInfoAction) action.get(packet);
            ents = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) entries.get(packet);
        } catch (IllegalAccessException ex) {
            return;
        }


        if(act != PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER) return;

        GameProfile profile = null;
        for(PacketPlayOutPlayerInfo.PlayerInfoData ent : ents) {
            for(MPlayer u : MidnightCoreAPI.getInstance().getPlayerManager()) {
                if(u.getUUID().equals(ent.a().getId())) {
                    profile = ent.a();
                    break;
                }
            }
            if(profile != null) break;
        }

        if(profile == null) return;
        if(skin == null) return;

        GameProfile oldProfile = profile;

        profile = new GameProfile(oldProfile.getId(), oldProfile.getName());

        Player pl = Bukkit.getPlayer(profile.getId());
        if(pl == null) return;

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();

        profile.getProperties().clear();
        profile.getProperties().putAll(epl.getProfile().getProperties());

        profile.getProperties().get("textures").clear();
        profile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));

        ents.set(0, packet.new PlayerInfoData(
                profile,
                epl.ping,
                epl.playerInteractManager.getGameMode(),
                epl.listName
        ));
    }*/

}
