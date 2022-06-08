package org.wallentines.midnightcore.spigot.adapter;

public class AdapterManager {


    private static SpigotAdapter CACHED_ADAPTER;

    public static void setAdapter(SpigotAdapter adapter) {

        CACHED_ADAPTER = adapter;
    }

    public static SpigotAdapter getAdapter() {

        return CACHED_ADAPTER;
    }

}
