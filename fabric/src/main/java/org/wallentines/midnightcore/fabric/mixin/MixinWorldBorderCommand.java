package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.WorldBorderCommand;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.wallentines.midnightcore.fabric.level.DynamicLevelContext;

@Mixin(WorldBorderCommand.class)
public class MixinWorldBorderCommand {


    @Redirect(method="setDamageBuffer", at=@At(value = "INVOKE", target="Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;"))
    private static ServerLevel redirectDamageBuffer(MinecraftServer instance, CommandSourceStack commandSourceStack) {
        return getLevel(instance, commandSourceStack);
    }

    @Redirect(method="setDamageAmount", at=@At(value = "INVOKE", target="Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;"))
    private static ServerLevel redirectDamageAmount(MinecraftServer instance, CommandSourceStack commandSourceStack) {
        return getLevel(instance, commandSourceStack);
    }

    @Redirect(method="setWarningTime", at=@At(value = "INVOKE", target="Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;"))
    private static ServerLevel redirectWarningTime(MinecraftServer instance, CommandSourceStack commandSourceStack) {
        return getLevel(instance, commandSourceStack);
    }

    @Redirect(method="setWarningDistance", at=@At(value = "INVOKE", target="Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;"))
    private static ServerLevel redirectWarningDistance(MinecraftServer instance, CommandSourceStack commandSourceStack) {
        return getLevel(instance, commandSourceStack);
    }

    @Redirect(method="getSize", at=@At(value = "INVOKE", target="Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;"))
    private static ServerLevel redirectGetSize(MinecraftServer instance, CommandSourceStack commandSourceStack) {
        return getLevel(instance, commandSourceStack);
    }

    @Redirect(method="setCenter", at=@At(value = "INVOKE", target="Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;"))
    private static ServerLevel redirectCenter(MinecraftServer instance, CommandSourceStack commandSourceStack) {
        return getLevel(instance, commandSourceStack);
    }

    @Redirect(method="setSize", at=@At(value = "INVOKE", target="Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;"))
    private static ServerLevel redirectSetSize(MinecraftServer instance, CommandSourceStack commandSourceStack) {
        return getLevel(instance, commandSourceStack);
    }

    private static ServerLevel getLevel(MinecraftServer instance, CommandSourceStack source) {

        ServerLevel lvl = source.getLevel();
        if(!(lvl instanceof DynamicLevelContext.DynamicLevel dl)) {
            return instance.overworld();
        }

        return instance.getLevel(dl.getRoot());
    }


}
