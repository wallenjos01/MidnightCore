package org.wallentines.midnightcore.fabric.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.text.LangProvider;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightlib.module.Module;

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

    public static <T extends Module<MidnightCoreAPI>> T getModule(Class<T> clazz) throws CommandRuntimeException {

        return getModule(clazz, Component.literal("A module of class " + clazz.getName() + " could not be found!"));
    }
    public static <T extends Module<MidnightCoreAPI>> T getModule(Class<T> clazz, Component message) throws CommandRuntimeException {

        T mod = MidnightCoreAPI.getModule(clazz);
        if(mod == null) throw new CommandRuntimeException(message);

        return mod;
    }

}
