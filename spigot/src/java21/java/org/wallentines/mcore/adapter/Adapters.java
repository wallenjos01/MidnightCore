package org.wallentines.mcore.adapter;

import org.bukkit.plugin.Plugin;

public class Adapters {

    public static Adapter findAdapter(String apiVersion, Plugin plugin) {
        return switch (apiVersion) {
            case "v1_20_R4" -> new org.wallentines.mcore.adapter.v1_20_R4.AdapterImpl();
            default -> null;
        };

    }

}
