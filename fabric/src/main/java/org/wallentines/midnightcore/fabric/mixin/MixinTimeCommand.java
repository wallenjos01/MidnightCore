package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DerivedLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.wallentines.midnightcore.fabric.MidnightCore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(TimeCommand.class)
public class MixinTimeCommand {

    private static CommandSourceStack midnight_core_currentStack;

    @Inject(method = "setTime", at = @At("HEAD"))
    private static void onSetHead(CommandSourceStack commandSourceStack, int i, CallbackInfoReturnable<Integer> cir) {
        midnight_core_currentStack = commandSourceStack;
    }

    @ModifyVariable(method = "setTime", at=@At(value = "STORE"), ordinal = 0)
    private static Iterator<?> onSet(Iterator<?> orig) {
        return getLevel(midnight_core_currentStack).iterator();
    }

    @Inject(method = "addTime", at = @At("HEAD"))
    private static void onAddHead(CommandSourceStack commandSourceStack, int i, CallbackInfoReturnable<Integer> cir) {
        midnight_core_currentStack = commandSourceStack;
    }

    @ModifyVariable(method = "addTime", at=@At(value = "STORE"), ordinal = 0)
    private static Iterator<?> onAdd(Iterator<?> orig) {
        return getLevel(midnight_core_currentStack).iterator();
    }

    private static Iterable<Level> getLevel(CommandSourceStack stack) {

        ServerLevel level = stack.getLevel();

        List<Level> levels = new ArrayList<>();
        levels.add(level);

        for(Level l : level.getServer().getAllLevels()) {
            if(l == level) continue;

            if(l.getLevelData() == level.getLevelData() || (level.getLevelData() instanceof DerivedLevelData && ((AccessorDerivedLevelData) level.getLevelData()).getWrapped() == l.getLevelData())) {

                levels.add(l);
            }
        }

        return levels;

    }

}
