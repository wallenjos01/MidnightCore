package org.wallentines.mcore.util;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;
import org.wallentines.mcore.MidnightCoreAPI;

import java.lang.reflect.InvocationTargetException;

public class CommandUtil {


    public static void registerCommand(Plugin plugin, Command cmd) {

        try {
            CommandMap cm = (CommandMap) Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap").invoke(Bukkit.getServer());
            cm.register(plugin.getName(), cmd);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            MidnightCoreAPI.LOGGER.error("Unable to register command!", ex);
        }
    }

}
