package org.wallentines.mcore.adapter.v1_16_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.adapter.SkinUpdater;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class SkinUpdaterImpl implements SkinUpdater {

    private void setProfileSkin(GameProfile profile, @Nullable Skin skin) {
        profile.getProperties().get("textures").clear();
        if(skin != null) {
            profile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
        }
    }

    @Override
    public void changePlayerSkin(Player player, @Nullable Skin skin) {

        EntityPlayer epl = ((CraftPlayer) player).getHandle();
        GameProfile gameProfile = ((CraftPlayer) player).getProfile(); // getGameProfile()
        setProfileSkin(gameProfile, skin);

        MinecraftServer server = epl.server;
        if(server == null) return;

        // Make sure player is ready to receive a respawn packet
        player.leaveVehicle();

        // Store velocity so it can be re-applied later
        Vec3D velocity = epl.getMot();

        // Create Packets
        PacketPlayOutPlayerInfo remove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, epl);
        PacketPlayOutPlayerInfo add =  new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, epl);

        List<Pair<EnumItemSlot, ItemStack>> items = Arrays.stream(EnumItemSlot.values()).map(eis -> new Pair<>(eis, epl.getEquipment(eis))).collect(Collectors.toList());

        int entityId = epl.getId();
        PacketPlayOutEntityEquipment equip = new PacketPlayOutEntityEquipment(entityId, items);

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

        Location location = player.getLocation();
        PacketPlayOutPosition position = new PacketPlayOutPosition(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), new HashSet<>(), 0);

        // Player information should be sent to everyone
        for(EntityPlayer obs : server.getPlayerList().players) { // getPlayerList(), getPlayers()

            obs.playerConnection.sendPacket(remove);
            obs.playerConnection.sendPacket(add);
        }

        // Entity information should be sent to observers in the same world
        List<EntityPlayer> observers = world.a(pl -> pl != epl);

        if(!observers.isEmpty()) {

            PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(entityId);
            PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(epl);
            PacketPlayOutEntityMetadata tracker = new PacketPlayOutEntityMetadata(epl.getId(), epl.getDataWatcher(), true);

            float headRot = player.getEyeLocation().getYaw();
            int rot = (int) headRot;
            if(headRot < (float) rot) rot -= 1;
            PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation(epl, (byte) ((rot * 256.0f) / 360.0f));


            for(EntityPlayer obs : observers) {

                obs.playerConnection.sendPacket(destroy);
                obs.playerConnection.sendPacket(spawn);
                obs.playerConnection.sendPacket(head);
                obs.playerConnection.sendPacket(tracker);

                obs.playerConnection.sendPacket(equip);
            }
        }

        // The remaining packets should only be sent to the updated player
        epl.playerConnection.sendPacket(respawn);
        epl.playerConnection.sendPacket(position);

        epl.playerConnection.sendPacket(equip);

        server.getPlayerList().updateClient(epl);
        epl.updateAbilities();

        epl.setMot(velocity);
        epl.playerConnection.sendPacket(new PacketPlayOutEntityVelocity(epl));

    }
}
