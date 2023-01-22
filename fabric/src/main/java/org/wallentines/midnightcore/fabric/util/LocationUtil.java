package org.wallentines.midnightcore.fabric.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.ChunkPos;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.fabric.MidnightCore;
import org.wallentines.midnightlib.math.Vec3d;


public class LocationUtil {

    public static Location getEntityLocation(Entity ent) {

        return new Location(ConversionUtil.toIdentifier(ent.getLevel().dimension().location()), new Vec3d(ent.getX(), ent.getY(), ent.getZ()), ent.getRotationVector().y, ent.getRotationVector().x);
    }

    public static Location getPlayerSpawnLocation(ServerPlayer player) {

        BlockPos spawn = player.getRespawnPosition();
        if(spawn == null) return getSpawnLocation(MidnightCore.getInstance().getServer().overworld());

        return new Location(ConversionUtil.toIdentifier(player.getRespawnDimension().location()), new Vec3d(spawn.getX(), spawn.getY(), spawn.getZ()), player.getRespawnAngle(), 0.0f);
    }

    public static Location getSpawnLocation(ServerLevel world) {

        BlockPos spawn = world.getSharedSpawnPos();
        return new Location(ConversionUtil.toIdentifier(world.dimension().location()), new Vec3d(spawn.getX(), spawn.getY(), spawn.getZ()), world.getSharedSpawnAngle(), 0.0f);
    }


    public static void teleport(Entity ent, Location location) {

        ServerLevel world = getLevel(location);
        if(world == null) {
            MidnightCoreAPI.getLogger().warn("Unable to teleport entity! World does not exist!");
            return;
        }

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        float yaw = location.getYaw();
        float pitch = location.getPitch();

        BlockPos pos = new BlockPos(x,y,z);

        if(!world.isInWorldBounds(pos)) {
            MidnightCoreAPI.getLogger().warn("Unable to teleport entity! Position is invalid!");
            return;
        }

        if(ent instanceof ServerPlayer player) {

            ChunkPos chunkPos = new ChunkPos(pos);
            world.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkPos, 1, ent.getId());

            ent.stopRiding();
            if(player.isSleeping()) {
                player.stopSleepInBed(true, true);
            }

            if(world == player.getLevel()) {
                player.connection.teleport(x, y, z, yaw, pitch);
            } else {
                player.teleportTo(world, x, y, z, yaw, pitch);
            }

            player.setYHeadRot(yaw);

        } else {

            float wrappedYaw = Mth.wrapDegrees(yaw);
            float wrappedPitch = Mth.clamp(Mth.wrapDegrees(pitch), -90.0f, 90.0f);

            if(world == ent.level) {

                ent.moveTo(x, y, z, wrappedYaw,  wrappedPitch);
                ent.setYHeadRot(wrappedYaw);

            } else {

                ent.unRide();
                Entity oldEnt = ent;
                ent = ent.getType().create(world);

                if(ent == null) {
                    MidnightCoreAPI.getLogger().warn("Unable to teleport entity to new world!");
                    return;
                }

                ent.restoreFrom(ent);
                ent.moveTo(x, y, z, wrappedYaw, wrappedPitch);
                ent.setYHeadRot(wrappedYaw);

                oldEnt.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
                world.addDuringTeleport(ent);

            }
        }

        if(!(ent instanceof LivingEntity) || !((LivingEntity) ent).isFallFlying()) {
            ent.setDeltaMovement(ent.getDeltaMovement().multiply(1.0d, 0.0d, 1.0d));
            ent.setOnGround(true);
        }

        if(ent instanceof PathfinderMob) {
            ((PathfinderMob) ent).getNavigation().stop();
        }
    }

    public static ServerLevel getLevel(Location location) {

        return MidnightCore.getInstance().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, ConversionUtil.toResourceLocation(location.getWorldId())));
    }

}
