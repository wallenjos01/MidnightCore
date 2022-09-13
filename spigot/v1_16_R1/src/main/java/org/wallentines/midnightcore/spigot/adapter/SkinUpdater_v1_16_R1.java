package org.wallentines.midnightcore.spigot.adapter;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.EmptyByteBuf;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.wallentines.midnightcore.api.module.skin.Skin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SkinUpdater_v1_16_R1 implements SkinUpdater {

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

        List<Pair<EnumItemSlot, ItemStack>> items = new ArrayList<>();
        
        items.add(new Pair<>(EnumItemSlot.MAINHAND, epl.getItemInMainHand()));
        items.add(new Pair<>(EnumItemSlot.OFFHAND, epl.getItemInOffHand()));
        items.add(new Pair<>(EnumItemSlot.HEAD, epl.getEquipment(EnumItemSlot.HEAD)));
        items.add(new Pair<>(EnumItemSlot.CHEST, epl.getEquipment(EnumItemSlot.CHEST)));
        items.add(new Pair<>(EnumItemSlot.LEGS, epl.getEquipment(EnumItemSlot.LEGS)));
        items.add(new Pair<>(EnumItemSlot.FEET, epl.getEquipment(EnumItemSlot.FEET)));

        PacketPlayOutEntityEquipment equip = new PacketPlayOutEntityEquipment(epl.getId(), items);

        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(epl.getId());
        PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(epl);
        PacketPlayOutEntityMetadata tracker = new PacketPlayOutEntityMetadata(epl.getId(), epl.getDataWatcher(), true);

        float headRot = spl.getEyeLocation().getYaw();
        int rot = (int) headRot;
        if (headRot < (float) rot) rot -= 1;
        PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation(epl, (byte) ((rot * 256.0F) / 360.0F));

        WorldServer world = (WorldServer) epl.world;

        PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(
                world.getTypeKey(),
                world.getDimensionKey(),
                BiomeManager.a(world.getSeed()),
                epl.playerInteractManager.getGameMode(),
                epl.playerInteractManager.c(),
                world.isDebugWorld(),
                world.isFlatWorld(),
                true
        );

        PacketPlayOutPosition position = new PacketPlayOutPosition(spl.getLocation().getX(), spl.getLocation().getY(), spl.getLocation().getZ(), spl.getLocation().getYaw(), spl.getLocation().getPitch(), new HashSet<>(), 0);
        PacketPlayOutAbilities abilities = new PacketPlayOutAbilities(epl.abilities);

        // Send Packets
        for(EntityPlayer obs : server.getPlayerList().players) {

            obs.playerConnection.sendPacket(remove);
            obs.playerConnection.sendPacket(add);

            if(obs == epl || !epl.world.equals(obs.world)) continue;

            obs.playerConnection.sendPacket(destroy);
            obs.playerConnection.sendPacket(spawn);
            obs.playerConnection.sendPacket(head);
            obs.playerConnection.sendPacket(tracker);
            obs.playerConnection.sendPacket(equip);

        }

        epl.playerConnection.sendPacket(respawn);
        epl.playerConnection.sendPacket(position);
        epl.playerConnection.sendPacket(abilities);
        epl.playerConnection.sendPacket(equip);

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
}
