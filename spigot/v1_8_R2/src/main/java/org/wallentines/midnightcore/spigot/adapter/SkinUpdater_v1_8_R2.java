package org.wallentines.midnightcore.spigot.adapter;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.EmptyByteBuf;
import net.minecraft.server.v1_8_R2.*;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.wallentines.midnightcore.api.module.skin.Skin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class SkinUpdater_v1_8_R2 implements SkinUpdater {

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public void updateSkin(Player spl, Skin skin) {

        EntityPlayer epl = ((CraftPlayer) spl).getHandle();

        MinecraftServer server = epl.server;
        if(server == null) return;

        // Create Packets

        PacketPlayOutPlayerInfo remove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, epl);
        PacketPlayOutPlayerInfo add = createPacket(epl, skin);

        PacketPlayOutEntityEquipment equipM = new PacketPlayOutEntityEquipment(epl.getId(), 0, epl.inventory.getItemInHand());
        PacketPlayOutEntityEquipment equipH = new PacketPlayOutEntityEquipment(epl.getId(), 4, epl.getEquipment(4));
        PacketPlayOutEntityEquipment equipC = new PacketPlayOutEntityEquipment(epl.getId(), 3, epl.getEquipment(3));
        PacketPlayOutEntityEquipment equipL = new PacketPlayOutEntityEquipment(epl.getId(), 2, epl.getEquipment(2));
        PacketPlayOutEntityEquipment equipF = new PacketPlayOutEntityEquipment(epl.getId(), 1, epl.getEquipment(1));

        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(epl.getId());
        PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(epl);
        PacketPlayOutEntityMetadata tracker = new PacketPlayOutEntityMetadata(epl.getId(), epl.getDataWatcher(), true);

        float headRot = spl.getEyeLocation().getYaw();
        int rot = (int) headRot;
        if (headRot < (float) rot) rot -= 1;
        PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation(epl, (byte) ((rot * 256.0F) / 360.0F));

        WorldServer world = (WorldServer) epl.world;

        PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(world.dimension, world.getDifficulty(), world.getWorldData().getType(), epl.playerInteractManager.getGameMode());

        PacketPlayOutPosition position = new PacketPlayOutPosition(spl.getLocation().getX(), spl.getLocation().getY(), spl.getLocation().getZ(), spl.getLocation().getYaw(), spl.getLocation().getPitch(), new HashSet<>());
        PacketPlayOutAbilities abilities = new PacketPlayOutAbilities(epl.abilities);

        // Send Packets
        for(EntityPlayer obs : server.getPlayerList().players) {

            obs.playerConnection.sendPacket(remove);
            obs.playerConnection.sendPacket(add);

            if(!epl.world.equals(obs.world)) continue;

            obs.playerConnection.sendPacket(equipM);
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
        serializer.b(1);
        serializer.a(player.getProfile().getId());
        serializer.a(player.getProfile().getName());
        serializer.b(1);
        serializer.a("textures");
        serializer.a(skin.getValue());
        serializer.writeBoolean(true);
        serializer.a(skin.getSignature());
        serializer.a(player.playerInteractManager.getGameMode());
        serializer.b(player.ping);
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
