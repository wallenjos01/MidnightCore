package org.wallentines.mcore.adapter.v1_8_R2;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R2.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.adapter.SkinUpdater;

import java.util.HashSet;
import java.util.Iterator;
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
        epl.p(); // stopRiding()
        player.closeInventory(); // Clients do not close their own inventory before 1.16

        // Store velocity so it can be re-applied later
        Vec3D velocity = new Vec3D(epl.motX, epl.motY, epl.motZ);

        // Create Packets
        PacketPlayOutPlayerInfo remove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, epl);
        PacketPlayOutPlayerInfo add =  new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, epl);

        int entityId = epl.getId();

        PacketPlayOutEntityEquipment equipM = new PacketPlayOutEntityEquipment(entityId, 0, epl.inventory.getItemInHand());
        PacketPlayOutEntityEquipment equipF = new PacketPlayOutEntityEquipment(entityId, 1, epl.getEquipment(1));
        PacketPlayOutEntityEquipment equipL = new PacketPlayOutEntityEquipment(entityId, 2, epl.getEquipment(2));
        PacketPlayOutEntityEquipment equipC = new PacketPlayOutEntityEquipment(entityId, 3, epl.getEquipment(3));
        PacketPlayOutEntityEquipment equipH = new PacketPlayOutEntityEquipment(entityId, 4, epl.getEquipment(4));

        WorldServer world = (WorldServer) epl.world;

        PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(
                world.dimension,
                world.getDifficulty(),
                world.getWorldData().getType(),
                epl.playerInteractManager.getGameMode()
        );

        Location location = player.getLocation();
        PacketPlayOutPosition position = new PacketPlayOutPosition(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), new HashSet<>());

        // Player information should be sent to everyone
        for(EntityPlayer obs : server.getPlayerList().players) { // getPlayerList(), getPlayers()

            obs.playerConnection.sendPacket(remove);
            obs.playerConnection.sendPacket(add);
        }

        // Entity information should be sent to observers in the same world
        List<EntityPlayer> observers = world.b(EntityPlayer.class, a -> a != epl);

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
        epl.playerConnection.sendPacket(equipF);
        epl.playerConnection.sendPacket(equipL);
        epl.playerConnection.sendPacket(equipC);
        epl.playerConnection.sendPacket(equipH);

        server.getPlayerList().updateClient(epl);
        epl.updateAbilities();

        for (MobEffect mobeffect : epl.getEffects()) {
            epl.playerConnection.sendPacket(new PacketPlayOutEntityEffect(entityId, mobeffect));
        }

        epl.motX = velocity.a;
        epl.motY = velocity.b;
        epl.motZ = velocity.c;
        epl.playerConnection.sendPacket(new PacketPlayOutEntityVelocity(epl));

    }
}
