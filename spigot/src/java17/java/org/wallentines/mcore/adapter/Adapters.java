package org.wallentines.mcore.adapter;

import org.bukkit.plugin.Plugin;

public class Adapters {

    public static Adapter findAdapter(String apiVersion, Plugin plugin) {

        return switch (apiVersion) {
            case "v1_17_R1" -> new org.wallentines.mcore.adapter.v1_17_R1.AdapterImpl();
            case "v1_18_R1" -> new org.wallentines.mcore.adapter.v1_18_R1.AdapterImpl();
            case "v1_18_R2" -> new org.wallentines.mcore.adapter.v1_18_R2.AdapterImpl();
            case "v1_19_R1" -> {
                // 1.19 has a couple differences in implementation to 1.19.1 and 1.19.2, even though they share the
                // same API version
                if(org.wallentines.mcore.adapter.v1_19_R1.VersionUtil.getGameVersion().getProtocolVersion() == 759) {
                    yield new org.wallentines.mcore.adapter.v1_19_R1.AdapterImpl();
                } else {
                    yield new org.wallentines.mcore.adapter.v1_19_R1v2.AdapterImpl();
                }
            }
            case "v1_19_R2" -> new org.wallentines.mcore.adapter.v1_19_R2.AdapterImpl();
            case "v1_19_R3" -> new org.wallentines.mcore.adapter.v1_19_R3.AdapterImpl();
            case "v1_20_R1" -> new org.wallentines.mcore.adapter.v1_20_R1.AdapterImpl();
            case "v1_20_R2" -> new org.wallentines.mcore.adapter.v1_20_R2.AdapterImpl();
            case "v1_20_R3" -> new org.wallentines.mcore.adapter.v1_20_R3.AdapterImpl();
            default -> null;
        };

    }

}
