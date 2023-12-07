package org.wallentines.mcore.adapter;


import org.bukkit.plugin.Plugin;

public class Adapters {

    public static Adapter findAdapter(String apiVersion, Plugin plugin) {

        switch (apiVersion) {
            case "v1_8_R1":
                return new org.wallentines.mcore.adapter.v1_8_R1.AdapterImpl();
            case "v1_8_R2":
                return new org.wallentines.mcore.adapter.v1_8_R2.AdapterImpl();
            case "v1_8_R3":
                return new org.wallentines.mcore.adapter.v1_8_R3.AdapterImpl();
            case "v1_9_R1":
                return new org.wallentines.mcore.adapter.v1_9_R1.AdapterImpl();
            case "v1_9_R2":
                return new org.wallentines.mcore.adapter.v1_9_R2.AdapterImpl();
            case "v1_10_R1":
                return new org.wallentines.mcore.adapter.v1_10_R1.AdapterImpl();
            case "v1_11_R1":
                return new org.wallentines.mcore.adapter.v1_11_R1.AdapterImpl();
            case "v1_12_R1":
                return new org.wallentines.mcore.adapter.v1_12_R1.AdapterImpl();
            case "v1_13_R1":
                return new org.wallentines.mcore.adapter.v1_13_R1.AdapterImpl();
            case "v1_13_R2": {
                // 1.13.1 has a couple differences in implementation to 1.13.2+, even though they share the same API
                // version
                if(org.wallentines.mcore.adapter.v1_13_R2.VersionUtil.getGameVersion().getId().equals("1.13.1")) {
                    return new org.wallentines.mcore.adapter.v1_13_R2.AdapterImpl();
                } else {
                    return new org.wallentines.mcore.adapter.v1_13_R2v2.AdapterImpl();
                }
            }
            case "v1_14_R1":
                return new org.wallentines.mcore.adapter.v1_14_R1.AdapterImpl();
            case "v1_15_R1":
                return new org.wallentines.mcore.adapter.v1_15_R1.AdapterImpl();
            case "v1_16_R1":
                return new org.wallentines.mcore.adapter.v1_16_R1.AdapterImpl();
            case "v1_16_R2":
                return new org.wallentines.mcore.adapter.v1_16_R2.AdapterImpl();
            case "v1_16_R3":
                return new org.wallentines.mcore.adapter.v1_16_R3.AdapterImpl();
            default:
                return new GenericAdapter(plugin);
        }
    }

}
