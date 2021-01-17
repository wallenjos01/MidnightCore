package me.m1dnightninja.midnightcore.fabric.api;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.math.Vec3d;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class Location {

    private double x, y, z;
    private float yaw, pitch;
    private ResourceLocation worldId;

    public Location(ResourceLocation worldId, double x, double y, double z) {
        this(worldId, x, y, z, 0.0f, 0.0f);
    }

    public Location(ResourceLocation worldId, double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.worldId = worldId;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public ServerLevel getWorld() {
        return MidnightCore.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, worldId));
    }

    public ResourceLocation getWorldId() {
        return worldId;
    }

    public void setWorld(ResourceLocation worldId) {
        this.worldId = worldId;
    }

    public BlockPos toBlockPos() {
        return new BlockPos(x,y,z);
    }

    public void teleport(Entity ent) {

        ServerLevel world = getWorld();
        if(world == null) {
            MidnightCoreAPI.getLogger().warn("Unable to teleport entity! World does not exist!");
            return;
        }

        BlockPos pos = toBlockPos();

        if(!Level.isInWorldBounds(pos)) {
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

        } else {


            float wrappedYaw = Mth.wrapDegrees(yaw);
            float wrappedPitch = Mth.clamp(Mth.wrapDegrees(pitch), -90.0f, 90.0f);

            if(world == ent.level) {

                ent.moveTo(x, y, z, wrappedYaw, wrappedPitch);
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

                world.addFromAnotherDimension(ent);
                oldEnt.removed = true;

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

    public Vec3d toVector() {
        return new Vec3d(x,y,z);
    }


    public static Location getEntityLocation(Entity ent) {

        return new Location(ent.level.dimension().location(), ent.getX(), ent.getY(), ent.getZ(), ent.yRot, ent.xRot);
    }

    public static Location getPlayerSpawnLocation(ServerPlayer player) {

        BlockPos spawn = player.getRespawnPosition();
        if(spawn == null) return getSpawnLocation(MidnightCore.getServer().overworld());

        return new Location(player.getRespawnDimension().location(), spawn.getX(), spawn.getY(), spawn.getZ(), player.getRespawnAngle(), 0.0f);
    }

    public static Location getSpawnLocation(ServerLevel world) {

        BlockPos spawn = world.getSharedSpawnPos();
        return new Location(world.dimension().location(), spawn.getX(), spawn.getY(), spawn.getZ(), world.getSharedSpawnAngle(), 0.0f);
    }

}
