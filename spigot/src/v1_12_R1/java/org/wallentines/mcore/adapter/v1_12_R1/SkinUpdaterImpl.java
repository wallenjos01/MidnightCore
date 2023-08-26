package org.wallentines.mcore.adapter.v1_12_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.adapter.SkinUpdater;

import java.util.HashSet;
import java.util.List;

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
        player.leaveVehicle(); // stopRiding()
        player.closeInventory(); // Clients do not close their own inventory before 1.16

        // Store velocity so it can be re-applied later
        Vec3D velocity = new Vec3D(epl.motX, epl.motY, epl.motZ);

        // Create Packets
        PacketPlayOutPlayerInfo remove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, epl);
        PacketPlayOutPlayerInfo add =  new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, epl);

        int entityId = epl.getId();

        PacketPlayOutEntityEquipment equipM = new PacketPlayOutEntityEquipment(entityId, EnumItemSlot.MAINHAND, epl.getItemInMainHand());
        PacketPlayOutEntityEquipment equipO = new PacketPlayOutEntityEquipment(entityId, EnumItemSlot.OFFHAND, epl.getItemInOffHand());
        PacketPlayOutEntityEquipment equipF = new PacketPlayOutEntityEquipment(entityId, EnumItemSlot.FEET, epl.getEquipment(EnumItemSlot.FEET));
        PacketPlayOutEntityEquipment equipL = new PacketPlayOutEntityEquipment(entityId, EnumItemSlot.LEGS, epl.getEquipment(EnumItemSlot.LEGS));
        PacketPlayOutEntityEquipment equipC = new PacketPlayOutEntityEquipment(entityId, EnumItemSlot.CHEST, epl.getEquipment(EnumItemSlot.CHEST));
        PacketPlayOutEntityEquipment equipH = new PacketPlayOutEntityEquipment(entityId, EnumItemSlot.HEAD, epl.getEquipment(EnumItemSlot.HEAD));

        WorldServer world = (WorldServer) epl.world;

        PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(
                world.dimension,
                world.getDifficulty(),
                world.getWorldData().getType(),
                epl.playerInteractManager.getGameMode()
        );

        Location location = player.getLocation();
        PacketPlayOutPosition position = new PacketPlayOutPosition(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), new HashSet<>(), 0);

        // Player information should be sent to everyone
        for(EntityPlayer obs : server.getPlayerList().players) { // getPlayerList(), getPlayers()

            obs.playerConnection.sendPacket(remove);
            obs.playerConnection.sendPacket(add);
        }

        // Entity information should be sent to observers in the same world
        List<EntityPlayer> observers = world.b(EntityPlayer.class, a -> true);

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

                obs.playerConnection.sendPacket(equipM);
                obs.playerConnection.sendPacket(equipO);
                obs.playerConnection.sendPacket(equipF);
                obs.playerConnection.sendPacket(equipL);
                obs.playerConnection.sendPacket(equipC);
                obs.playerConnection.sendPacket(equipH);
            }
        }

        // The remaining packets should only be sent to the updated player
        epl.playerConnection.sendPacket(respawn);
        epl.playerConnection.sendPacket(position);

        epl.playerConnection.sendPacket(equipM);
        epl.playerConnection.sendPacket(equipO);
        epl.playerConnection.sendPacket(equipF);
        epl.playerConnection.sendPacket(equipL);
        epl.playerConnection.sendPacket(equipC);
        epl.playerConnection.sendPacket(equipH);

        server.getPlayerList().updateClient(epl);
        epl.updateAbilities();

        epl.motX = velocity.x;
        epl.motY = velocity.y;
        epl.motZ = velocity.z;
        epl.playerConnection.sendPacket(new PacketPlayOutEntityVelocity(epl));

    }
}
