package org.wallentines.midnightcore.fabric.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import org.wallentines.midnightcore.api.module.lang.LangProvider;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;

public class CommandUtil {

    public static void sendCommandSuccess(CommandContext<CommandSourceStack> context, LangProvider langProvider, boolean notify, String key, Object... args) {

        MPlayer u = null;
        try {
            u = FabricPlayer.wrap(context.getSource().getPlayerOrException());

        } catch(CommandSyntaxException ex) {
            // Ignore
        }

        context.getSource().sendSuccess(ConversionUtil.toComponent(langProvider.getMessage(key, u, args)), notify);

    }

    public static void sendCommandFailure(CommandContext<CommandSourceStack> context, LangProvider langProvider, String key, Object... args) {

        MPlayer u = null;
        try {
            u = FabricPlayer.wrap(context.getSource().getPlayerOrException());

        } catch(CommandSyntaxException ex) {
            // Ignore
        }

        context.getSource().sendFailure(ConversionUtil.toComponent(langProvider.getMessage(key, u, args)));

    }

}
