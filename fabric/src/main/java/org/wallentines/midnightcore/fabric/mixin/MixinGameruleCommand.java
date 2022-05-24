package org.wallentines.midnightcore.fabric.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRuleCommand.class)
public class MixinGameruleCommand {

    private static CommandSourceStack midnight_core_src;
    private static GameRules.Key<?> midnight_core_activeKey;

    @Inject(method= "setRule", at=@At("HEAD"))
    private static <T extends GameRules.Value<T>> void onSet(CommandContext<CommandSourceStack> context, GameRules.Key<T> key, CallbackInfoReturnable<Integer> cir) {
        midnight_core_src = context.getSource();
        midnight_core_activeKey = key;
    }

    @ModifyVariable(method="setRule", at=@At("STORE"), ordinal = 0)
    private static <T extends GameRules.Value<T>> T onSetGetGameRule(T rule) {

        return getGameRule();
    }

    @Inject(method= "queryRule", at=@At("HEAD"))
    private static <T extends GameRules.Value<T>> void onQuery(CommandSourceStack source, GameRules.Key<T> key, CallbackInfoReturnable<Integer> cir) {
        midnight_core_src = source;
        midnight_core_activeKey = key;
    }

    @SuppressWarnings("executeQuery")
    @ModifyVariable(method="queryRule", at=@At("STORE"), ordinal = 0)
    private static <T extends GameRules.Value<T>> T onQueryGetGameRule(T rule) {

        return getGameRule();
    }

    @SuppressWarnings("unchecked")
    private static <T extends GameRules.Value<T>> T getGameRule() {
        T out;
        try {
            out = midnight_core_src.getPlayerOrException().getLevel().getGameRules().getRule((GameRules.Key<T>) midnight_core_activeKey);
        } catch(CommandSyntaxException ex) {
            out = midnight_core_src.getServer().getGameRules().getRule((GameRules.Key<T>) midnight_core_activeKey);
        }

        midnight_core_src = null;
        midnight_core_activeKey = null;

        return out;
    }

}
