package org.wallentines.mcore.adapter.v1_19_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.adapter.SkinUpdater;

import java.util.*;
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

        MinecraftServer server = epl.c;
        if(server == null) return;

        // Make sure player is ready to receive a respawn packet
        player.leaveVehicle();

        // Store velocity so it can be re-applied later
        Vec3D velocity = epl.da(); // getDeltaMovement()

        // Create Packets
        PacketPlayOutPlayerInfo remove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.c, epl);
        PacketPlayOutPlayerInfo add = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, epl); // createPlayerInitializing()

        List<Pair<EnumItemSlot, ItemStack>> items = Arrays.stream(EnumItemSlot.values()).map(eis -> new Pair<>(eis, epl.c(eis))).collect(Collectors.toList()); // getItemBySlot

        int entityId = epl.ae();
        PacketPlayOutEntityEquipment equip = new PacketPlayOutEntityEquipment(entityId, items);

        WorldServer world = (WorldServer) epl.s;

        PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(
                world.Z(),
                world.ab(),
                BiomeManager.a(player.getWorld().getSeed()), // obfuscateSeed(),
                epl.d.b(),
                epl.d.c(), // getPreviousGameModeForPlayer()
                world.ae(),
                world.A(),
                true,
                Optional.empty()
        );

        Location location = player.getLocation();
        PacketPlayOutPosition position = new PacketPlayOutPosition(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), Set.of(), 0, false);

        // Player information should be sent to everyone
        for(EntityPlayer obs : server.ac().j) { // getPlayerList(), getPlayers()

            obs.b.a(remove);
            obs.b.a(add);
        }

        // Entity information should be sent to observers in the same world
        Collection<EntityPlayer> observers = world.a(pl -> pl != epl);
        if(!observers.isEmpty()) {

            PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(entityId);
            PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(epl);

            PacketPlayOutEntityMetadata tracker = new PacketPlayOutEntityMetadata(entityId, epl.ai(), true);

            float headRot = player.getEyeLocation().getYaw();
            int rot = (int) headRot;
            if(headRot < (float) rot) rot -= 1;
            PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation(epl, (byte) ((rot * 256.0f) / 360.0f));


            for(EntityPlayer obs : observers) {

                obs.b.a(destroy);
                obs.b.a(spawn);
                obs.b.a(head);
                obs.b.a(equip);
                obs.b.a(tracker);
            }
        }

        // The remaining packets should only be sent to the updated player
        epl.b.a(respawn);
        epl.b.a(position);
        epl.b.a(equip);

        server.ac().d(epl);
        server.ac().e(epl);
        epl.w();

        epl.g(velocity); // setDeltaMovement()
        epl.b.a(new PacketPlayOutEntityVelocity(epl));

    }
}
