package org.wallentines.midnightcore.spigot.adapter;

import com.google.common.hash.Hashing;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.wallentines.midnightcore.api.module.skin.Skin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class SkinUpdater_v1_15_R1 implements SkinUpdater {

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public void updateSkin(Player spl, Skin skin) {

        EntityPlayer epl = ((CraftPlayer) spl).getHandle();

        MinecraftServer server = epl.server;
        if(server == null) return;

        // Clients do not close their own inventories on respawn before 1.16
        spl.closeInventory();

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

        long hash = Hashing.sha256().hashLong(world.getSeed()).asLong();
        PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(epl.dimension, hash, world.getWorldData().getType(), epl.playerInteractManager.getGameMode());

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

        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());

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
}
