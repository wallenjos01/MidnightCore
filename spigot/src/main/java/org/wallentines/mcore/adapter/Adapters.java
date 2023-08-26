package org.wallentines.mcore.adapter;

import org.bukkit.plugin.Plugin;

public class Adapters {


    public static Adapter findAdapter(String apiVersion, Plugin plugin) {

        return switch (apiVersion) {
            case "v1_17_R1" -> new org.wallentines.mcore.adapter.v1_17_R1.AdapterImpl();
            case "v1_18_R1" -> new org.wallentines.mcore.adapter.v1_18_R1.AdapterImpl();
            case "v1_18_R2" -> new org.wallentines.mcore.adapter.v1_18_R2.AdapterImpl();
            case "v1_19_R3" -> new org.wallentines.mcore.adapter.v1_19_R3.AdapterImpl();
            case "v1_20_R1" -> new org.wallentines.mcore.adapter.v1_20_R1.AdapterImpl();
            default -> new GenericAdapter(plugin);
        };

    }

}
