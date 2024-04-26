package org.wallentines.mcore.adapter;

import org.bukkit.plugin.Plugin;
import org.wallentines.mcore.MidnightCoreAPI;

public class Adapters {

    public static Adapter findAdapter(String apiVersion, Plugin plugin) {
        MidnightCoreAPI.LOGGER.warn("Default adapters class loaded! ");
        return new GenericAdapter(plugin);
    }

}
