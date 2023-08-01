package org.wallentines.mcore.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.wallentines.mcore.Entity;
import org.wallentines.mcore.Location;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ContentConverter;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mcore.util.RegistryUtil;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mixin(net.minecraft.world.entity.Entity.class)
public abstract class MixinEntity implements Entity {

    @Shadow public abstract UUID getUUID();

    @Shadow @Nullable public abstract MinecraftServer getServer();

    @Shadow private Level level;

    @Shadow public abstract boolean teleportTo(ServerLevel serverLevel, double d, double e, double f, Set<RelativeMovement> set, float g, float h);

    @Shadow public abstract void setDeltaMovement(Vec3 vec3);

    @Shadow public abstract Vec3 getDeltaMovement();

    @Shadow public abstract void setOnGround(boolean bl);

    @Shadow private Vec3 position;

    @Shadow private float xRot;

    @Shadow private float yRot;

    @Shadow @Nullable public abstract net.minecraft.network.chat.Component getCustomName();

    @Shadow public abstract String getStringUUID();

    @Override
    public Identifier getType() {

        net.minecraft.world.entity.Entity ent = (net.minecraft.world.entity.Entity) (Object) this;

        return RegistryUtil.registry(Registries.ENTITY_TYPE)
                .map(reg -> reg.getKey(ent.getType()))
                .map(ConversionUtil::toIdentifier)
                .orElseGet(() -> new Identifier("minecraft", "pig"));
    }

    @Unique
    @Override
    public Component getDisplayName() {

        net.minecraft.network.chat.Component comp = getCustomName();
        if(comp == null) return Component.text(getStringUUID());

        return ContentConverter.convertReverse(comp);
    }

    @Unique
    @Override
    public Identifier getDimensionId() {
        return ConversionUtil.toIdentifier(level.dimension().location());
    }

    @Unique
    @Override
    public Vec3d getPosition() {
        return new Vec3d(position.x, position.y, position.z);
    }

    @Unique
    @Override
    public float getYaw() {
        return yRot;
    }

    @Unique
    @Override
    public float getPitch() {
        return xRot;
    }

    @Unique
    @Override
    public void teleport(Location location) {

        if(level.isClientSide) {
            throw new IllegalStateException("Attempt to teleport a client-side entity!");
        }

        net.minecraft.world.entity.Entity self = (net.minecraft.world.entity.Entity) (Object) this;

        MinecraftServer server = getServer();
        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, ConversionUtil.toResourceLocation(location.dimension)));

        double x = location.position.getX();
        double y = location.position.getY();
        double z = location.position.getZ();

        float yaw = location.yaw;
        float pitch = location.pitch;

        BlockPos blockPos = BlockPos.containing(x, y, z);
        if (!Level.isInSpawnableBounds(blockPos)) {

            throw new IllegalArgumentException("Attempt to teleport Entity outside of maximum world boundaries!");

        } else {

            float wrappedYaw = Mth.wrapDegrees(yaw);
            float wrappedPitch = Mth.wrapDegrees(pitch);

            if (teleportTo(level, x, y, z, new HashSet<>(), wrappedYaw, wrappedPitch)) {

                if (self instanceof LivingEntity liv && !liv.isFallFlying()) {
                    setDeltaMovement(getDeltaMovement().multiply(1.0, 0.0, 1.0));
                    setOnGround(true);
                }

                if (self instanceof PathfinderMob path) {
                    path.getNavigation().stop();
                }
            }
        }
    }
}
