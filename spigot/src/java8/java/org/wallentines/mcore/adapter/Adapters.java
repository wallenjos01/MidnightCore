package org.wallentines.mcore.adapter;


import org.bukkit.plugin.Plugin;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.adapter.Adapter;

public class Adapters {

    public static Adapter findAdapter(GameVersion version, Plugin plugin) {

        switch (version.getProtocolVersion()) {
            case 47: {
                String[] parts = version.getId().split(".");
                int patch = parts.length < 3 ? 0 :  Integer.parseInt(parts[2]);
                if(patch < 3) {
                    return new org.wallentines.mcore.adapter.v1_8_R1.AdapterImpl();
                }
                else if(patch == 3) {
                    return new org.wallentines.mcore.adapter.v1_8_R2.AdapterImpl();
                }
                else {
                    return new org.wallentines.mcore.adapter.v1_8_R3.AdapterImpl();
                }
            }
            case 107:
                return new org.wallentines.mcore.adapter.v1_9_R1.AdapterImpl();
            case 110:
                return new org.wallentines.mcore.adapter.v1_9_R2.AdapterImpl();
            case 210:
                return new org.wallentines.mcore.adapter.v1_10_R1.AdapterImpl();
            case 316:
                return new org.wallentines.mcore.adapter.v1_11_R1.AdapterImpl();
            case 340:
                return new org.wallentines.mcore.adapter.v1_12_R1.AdapterImpl();
            case 393:
                return new org.wallentines.mcore.adapter.v1_13_R1.AdapterImpl();
            case 401:
                return new org.wallentines.mcore.adapter.v1_13_R2.AdapterImpl();
            case 404:
                return new org.wallentines.mcore.adapter.v1_13_R2v2.AdapterImpl();
            case 498:
                return new org.wallentines.mcore.adapter.v1_14_R1.AdapterImpl();
            case 578:
                return new org.wallentines.mcore.adapter.v1_15_R1.AdapterImpl();
            case 736:
                return new org.wallentines.mcore.adapter.v1_16_R1.AdapterImpl();
            case 751:
                return new org.wallentines.mcore.adapter.v1_16_R2.AdapterImpl();
            case 754:
                return new org.wallentines.mcore.adapter.v1_16_R3.AdapterImpl();
            default:
                return null;
        }
    }


    public static GameVersion estimateVersion(String apiVersion) {

        switch (apiVersion) {
            case "v1_8_R1":
                return new GameVersion("1.8.2", 47);
            case "v1_8_R2":
                return new GameVersion("1.8.3", 47);
            case "v1_8_R3":
                return new GameVersion("1.8.8", 47);
            case "v1_9_R1":
                return new GameVersion("1.9", 107);
            case "v1_9_R2":
                return new GameVersion("1.9.4", 110);
            case "v1_10_R1":
                return new GameVersion("1.10.2", 210);
            case "v1_11_R1":
                return new GameVersion("1.11.2", 316);
            case "v1_12_R1":
                return new GameVersion("1.12.2", 340);
            case "v1_13_R1":
                return new GameVersion("1.13", 393);
            case "v1_13_R2": {
                // 1.13.1 has a couple differences in implementation to 1.13.2+, even though they share the same API
                // version
                return org.wallentines.mcore.adapter.v1_13_R2.VersionUtil.getGameVersion();
            }
            case "v1_14_R1":
                return new GameVersion("1.14.4", 498);
            case "v1_15_R1":
                return new GameVersion("1.15.2", 578);
            case "v1_16_R1":
                return new GameVersion("1.16.1", 736);
            case "v1_16_R2":
                return new GameVersion("1.16.2", 751);
            case "v1_16_R3":
                return new GameVersion("1.16.4", 754);
            default:
                return null;
        }
    }

}
