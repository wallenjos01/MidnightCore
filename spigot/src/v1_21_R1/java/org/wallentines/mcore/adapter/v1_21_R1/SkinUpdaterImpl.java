package org.wallentines.mcore.adapter.v1_21_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.adapter.SkinUpdater;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;


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

        MinecraftServer server = epl.d;
        if (server == null) return;

        // Make sure player is ready to receive a respawn packet
        epl.Y(); // stopRiding()

        // Store velocity so it can be re-applied later
        Vec3D velocity = epl.dr();

        // Create Packets
        ClientboundPlayerInfoRemovePacket remove = new ClientboundPlayerInfoRemovePacket(List.of(player.getUniqueId()));
        ClientboundPlayerInfoUpdatePacket add = ClientboundPlayerInfoUpdatePacket.a(List.of(epl)); // createPlayerInitializing()

        List<Pair<EnumItemSlot, ItemStack>> items = Arrays.stream(EnumItemSlot.values()).map(eis -> new Pair<>(eis, epl.a(eis))).toList(); // getItemBySlot

        int entityId = epl.an(); // getId()
        PacketPlayOutEntityEquipment equip = new PacketPlayOutEntityEquipment(entityId, items);

        WorldServer world = epl.A(); // serverLevel()
        PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(epl.b(world), (byte) 3);

        PacketPlayOutExperience exp = new PacketPlayOutExperience(epl.cs, epl.cr, epl.cq);

        Location location = player.getLocation();
        PacketPlayOutPosition position = new PacketPlayOutPosition(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), Set.of(), 0);

        // Player information should be sent to everyone
        for (EntityPlayer obs : server.ah().t()) { // getPlayerList(), getPlayers()

            obs.c.b(remove); // connection, send
            obs.c.b(add);
        }

        // Entity information should be sent to observers in the same world
        Collection<EntityPlayer> observers = world.a(pl -> pl != epl);
        if (!observers.isEmpty()) {

            PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(entityId);

            Location loc = player.getLocation();
            PacketPlayOutSpawnEntity spawn = new PacketPlayOutSpawnEntity(
                    entityId,
                    player.getUniqueId(),
                    loc.getX(),
                    loc.getY(),
                    loc.getZ(),
                    loc.getYaw(),
                    loc.getPitch(),
                    epl.am(),
                    0,
                    velocity,
                    player.getEyeLocation().getYaw()
            );

            PacketPlayOutEntityMetadata tracker = null;
            List<DataWatcher.c<?>> entityData = epl.ar().c(); // DataValue<?>, getEntityData(), getNonDefaultValues()
            if (entityData != null) {
                tracker = new PacketPlayOutEntityMetadata(entityId, entityData);
            }

            float headRot = player.getEyeLocation().getYaw();
            int rot = (int) headRot;
            if (headRot < (float) rot) rot -= 1;
            PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation(epl, (byte) ((rot * 256.0f) / 360.0f));


            for (EntityPlayer obs : observers) {

                obs.c.b(destroy);
                obs.c.b(spawn);
                obs.c.b(head);
                obs.c.b(equip);
                if (tracker != null) obs.c.b(tracker);
            }
        }

        // The remaining packets should only be sent to the updated player
        epl.c.b(respawn);
        epl.c.b(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.o, 0.0f)); // LEVEL_CHUNKS_LOAD_START (13)

        server.ah().d(epl); // sendPlayerPermissionLevel
        server.ah().e(epl); // sendAllPlayerInfo

        epl.c.b(position);
        epl.c.b(equip);
        epl.c.b(exp);

        epl.w(); // onUpdateAbilities

        epl.f(velocity); // setDeltaMovement()
        epl.c.b(new PacketPlayOutEntityVelocity(epl));

        for (MobEffect mobeffect : epl.et()) {
            epl.c.a(new PacketPlayOutEntityEffect(entityId, mobeffect, true));
        }
    }
}
