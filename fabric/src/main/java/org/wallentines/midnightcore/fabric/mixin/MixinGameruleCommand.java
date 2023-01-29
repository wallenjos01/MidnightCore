package org.wallentines.midnightcore.fabric.mixin;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRuleCommand.class)
public class MixinGameruleCommand {

    @Redirect(method="setRule", at=@At(value="INVOKE", target="Lnet/minecraft/server/MinecraftServer;getGameRules()Lnet/minecraft/world/level/GameRules;"))
    private static GameRules redirectGetGameRules(MinecraftServer instance, CommandContext<CommandSourceStack> ctx) {

        return getGameRules(ctx.getSource());
    }

    @SuppressWarnings("executeQuery")
    @Redirect(method="queryRule", at=@At(value="INVOKE", target="Lnet/minecraft/server/MinecraftServer;getGameRules()Lnet/minecraft/world/level/GameRules;"))
    private static GameRules onQueryGetGameRule(MinecraftServer instance, CommandSourceStack stack) {

        return getGameRules(stack);
    }

    private static GameRules getGameRules(CommandSourceStack stack) {

        ServerPlayer spl = stack.getPlayer();
        if(spl == null) {
            return stack.getServer().overworld().getGameRules();
        }

        return spl.getLevel().getGameRules();
    }

}
