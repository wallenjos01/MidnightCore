package org.wallentines.mcore.adapter;

import org.bukkit.plugin.Plugin;
import org.wallentines.mcore.GameVersion;

public class Adapters {

    public static Adapter findAdapter(GameVersion version, Plugin plugin) {

        return switch (version.getProtocolVersion()) {
            case 766 -> new org.wallentines.mcore.adapter.v1_20_R4.AdapterImpl();
            default -> null;
        };

    }

    public static GameVersion estimateVersion(String apiVersion) {
        return switch (apiVersion) {
            case "1_20_R4" -> new GameVersion("1.20.6", 766);
            default -> null;
        };
    }

}
