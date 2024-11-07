package org.wallentines.mcore.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.LookAt;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(TeleportCommand.class)
public interface AccessorTeleportCommand {

    @Invoker("performTeleport")
    static void callPerformTeleport(CommandSourceStack source, Entity entity, ServerLevel level, double x, double y, double z, Set<Relative> relatives, float yaw, float pitch, @Nullable LookAt lookAt) {
        throw new UnsupportedOperationException();
    }

}
