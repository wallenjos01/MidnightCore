package org.wallentines.mcore.adapter;


import org.bukkit.plugin.Plugin;

public class Adapters {

    public static Adapter findAdapter(String apiVersion, Plugin plugin) {

        switch (apiVersion) {
            case "v1_8_R1":
                return new org.wallentines.mcore.adapter.v1_8_R1.AdapterImpl();
            case "v1_16_R3":
                return new org.wallentines.mcore.adapter.v1_16_R3.AdapterImpl();
        }

        return new GenericAdapter(plugin);
    }

}