package me.m1dnightninja.midnightcore.fabric.mixin;

import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DerivedLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Mixin(TimeCommand.class)
public class MixinTimeCommand {

    private static CommandSourceStack stack;

    @Inject(method = "setTime", at = @At("HEAD"))
    private static void onSetHead(CommandSourceStack commandSourceStack, int i, CallbackInfoReturnable<Integer> cir) {
        stack = commandSourceStack;
    }

    @ModifyVariable(method = "setTime", at=@At(value = "STORE"), ordinal = 0)
    private static Iterator<?> onSet(Iterator<?> orig) {
        return getLevel(stack).iterator();
    }

    @Inject(method = "addTime", at = @At("HEAD"))
    private static void onAddHead(CommandSourceStack commandSourceStack, int i, CallbackInfoReturnable<Integer> cir) {
        stack = commandSourceStack;
    }

    @ModifyVariable(method = "addTime", at=@At(value = "STORE"), ordinal = 0)
    private static Iterator<?> onAdd(Iterator<?> orig) {
        return getLevel(stack).iterator();
    }

    private static Iterable<Level> getLevel(CommandSourceStack stack) {

        ServerLevel level = stack.getLevel();

        for(Level l : MidnightCore.getServer().getAllLevels()) {

            if(l.getLevelData() == level.getLevelData() || (level.getLevelData() instanceof DerivedLevelData && ((AccessorDerivedLevelData) level.getLevelData()).getWrapped() == l.getLevelData())) {

                stack.sendSuccess(new TextComponent(l.dimension().location().toString()), false);
                return Collections.singleton(l);
            }
        }

        return Collections.singleton(level);

    }

}
