package org.wallentines.mcore.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record PermissionPredicate(String node, Optional<Integer> defaultOpLevel) implements EntitySubPredicate {


    public static final MapCodec<PermissionPredicate> CODEC =
            RecordCodecBuilder.mapCodec((instance) -> instance.group(
                    Codec.STRING.fieldOf("permission").forGetter(PermissionPredicate::node),
                    Codec.INT.optionalFieldOf("default_op_level").forGetter(PermissionPredicate::defaultOpLevel)
            ).apply(instance, PermissionPredicate::new));


    @Override
    public @NotNull MapCodec<? extends EntitySubPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
        if(defaultOpLevel.isPresent()) {
            return Permissions.check(entity, this.node, this.defaultOpLevel.get());
        }
        return Permissions.check(entity, this.node);
    }
}
