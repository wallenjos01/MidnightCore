package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.DerivedLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(TimeCommand.class)
public class MixinTimeCommand {

    @Redirect(method = "setTime", at=@At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getAllLevels()Ljava/lang/Iterable;"))
    private static Iterable<ServerLevel> redirectSet(MinecraftServer instance, CommandSourceStack stack) {
        return getLevel(stack);
    }

    @Redirect(method = "addTime", at=@At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getAllLevels()Ljava/lang/Iterable;"))
    private static Iterable<ServerLevel> redirectAdd(MinecraftServer instance, CommandSourceStack stack) {
        return getLevel(stack);
    }

    private static Iterable<ServerLevel> getLevel(CommandSourceStack stack) {

        ServerLevel level = stack.getLevel();

        List<ServerLevel> levels = new ArrayList<>();
        levels.add(level);

        for(ServerLevel l : level.getServer().getAllLevels()) {
            if(l == level) continue;

            if(l.getLevelData() == level.getLevelData() ||
                    level.getLevelData() instanceof DerivedLevelData &&
                    ((AccessorDerivedLevelData) level.getLevelData()).getWrapped() == l.getLevelData()) {

                levels.add(l);
            }
        }

        return levels;
    }

}
