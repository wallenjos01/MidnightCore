package org.wallentines.mcore.adapter;

import org.bukkit.plugin.Plugin;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.adapter.Adapter;

public class Adapters {

    public static Adapter findAdapter(GameVersion version, Plugin plugin) {

        return switch(version.getProtocolVersion()) {
            case 755,756 -> new org.wallentines.mcore.adapter.v1_17_R1.AdapterImpl(); // 1.17.x
            case 757 -> new org.wallentines.mcore.adapter.v1_18_R1.AdapterImpl(); // 1.18 / 1.18.1
            case 758 -> new org.wallentines.mcore.adapter.v1_18_R2.AdapterImpl(); // 1.18.2
            case 759 -> new org.wallentines.mcore.adapter.v1_19_R1.AdapterImpl(); // 1.19
            case 760 -> new org.wallentines.mcore.adapter.v1_19_R1v2.AdapterImpl(); // 1.19.2
            case 761-> new org.wallentines.mcore.adapter.v1_19_R2.AdapterImpl(); // 1.19.3
            case 762 -> new org.wallentines.mcore.adapter.v1_19_R3.AdapterImpl(); // 1.19.4
            case 763 -> new org.wallentines.mcore.adapter.v1_20_R1.AdapterImpl(); // 1.20 / 1.20.1
            case 764 -> new org.wallentines.mcore.adapter.v1_20_R2.AdapterImpl(); // 1.20.2
            case 765 -> new org.wallentines.mcore.adapter.v1_20_R3.AdapterImpl(); // 1.20.3 / 1.20.4
            default -> null;
        };

    }

    public static GameVersion estimateVersion(String apiVersion) {

        return switch (apiVersion) {
            case "v1_17_R1" -> new GameVersion("1.17.1", 756);
            case "v1_18_R1" -> new GameVersion("1.18.1", 757);
            case "v1_18_R2" -> new GameVersion("1.18.2", 758);
            // 1.19 and 1.19.1 have different adapters but the same API version
            case "v1_19_R1" -> org.wallentines.mcore.adapter.v1_19_R1.VersionUtil.getGameVersion();
            case "v1_19_R2" -> new GameVersion("1.19.3", 761);
            case "v1_19_R3" -> new GameVersion("1.19.4", 762);
            case "v1_20_R1" -> new GameVersion("1.20.1", 763);
            case "v1_20_R2" -> new GameVersion("1.20.2", 764);
            case "v1_20_R3" -> new GameVersion("1.20.4", 765);
            default -> null;
        };
    }

}
