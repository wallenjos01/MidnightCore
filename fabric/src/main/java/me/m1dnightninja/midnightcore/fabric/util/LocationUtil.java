package me.m1dnightninja.midnightcore.fabric.util;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.player.Location;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.ChunkPos;

public final class LocationUtil {

    public static Location getEntityLocation(Entity ent) {

        return new Location(ConversionUtil.fromResourceLocation(ent.level.dimension().location()), ent.getX(), ent.getY(), ent.getZ(), ent.getRotationVector().y, ent.getRotationVector().x);
    }

    public static Location getPlayerSpawnLocation(ServerPlayer player) {

        BlockPos spawn = player.getRespawnPosition();
        if(spawn == null) return getSpawnLocation(MidnightCore.getServer().overworld());

        return new Location(ConversionUtil.fromResourceLocation(player.getRespawnDimension().location()), spawn.getX(), spawn.getY(), spawn.getZ(), player.getRespawnAngle(), 0.0f);
    }

    public static Location getSpawnLocation(ServerLevel world) {

        BlockPos spawn = world.getSharedSpawnPos();
        return new Location(ConversionUtil.fromResourceLocation(world.dimension().location()), spawn.getX(), spawn.getY(), spawn.getZ(), world.getSharedSpawnAngle(), 0.0f);
    }


    public static void teleport(Entity ent, Location location) {

        ServerLevel world = MidnightCore.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, ConversionUtil.toResourceLocation(location.getWorld())));
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

        if(ent instanceof ServerPlayer) {

            ServerPlayer player = (ServerPlayer) ent;

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

}
