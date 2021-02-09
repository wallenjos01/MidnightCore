package me.m1dnightninja.midnightcore.fabric.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.TimeCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.Iterator;

@Mixin(TimeCommand.class)
public class MixinTimeCommand {

    private static CommandSourceStack stack;

    @Inject(method = "setTime", at = @At("HEAD"))
    private static void onSetHead(CommandSourceStack commandSourceStack, int i, CallbackInfoReturnable<Integer> cir) {
        stack = commandSourceStack;
    }

    @ModifyVariable(method = "setTime", at=@At(value = "STORE"), ordinal = 0)
    private static Iterator<?> onSet(Iterator<?> orig) {
        return Collections.singleton(stack.getLevel()).iterator();
    }

    @Inject(method = "addTime", at = @At("HEAD"))
    private static void onAddHead(CommandSourceStack commandSourceStack, int i, CallbackInfoReturnable<Integer> cir) {
        stack = commandSourceStack;
    }

    @ModifyVariable(method = "addTime", at=@At(value = "STORE"), ordinal = 0)
    private static Iterator<?> onAdd(Iterator<?> orig) {
        return Collections.singleton(stack.getLevel()).iterator();
    }

}
