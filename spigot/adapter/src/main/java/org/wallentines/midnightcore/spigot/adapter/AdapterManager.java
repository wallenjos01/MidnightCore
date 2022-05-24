package org.wallentines.midnightcore.spigot.adapter;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class AdapterManager {

    public static final String API_VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".",",").split(",")[3];
    private static final List<SpigotAdapter> ADAPTERS = new ArrayList<>();

    private static SpigotAdapter CACHED_ADAPTER;

    public static void register(SpigotAdapter adapter) {

        ADAPTERS.add(adapter);
    }

    public static SpigotAdapter getAdapter() {

        if(CACHED_ADAPTER != null) return CACHED_ADAPTER;

        for(SpigotAdapter ad : ADAPTERS) {

            if(ad.isVersionSupported(API_VERSION)) {
                CACHED_ADAPTER = ad;
                return ad;
            }
        }

        CACHED_ADAPTER = GenericAdapter.INSTANCE;
        return CACHED_ADAPTER;
    }

}
