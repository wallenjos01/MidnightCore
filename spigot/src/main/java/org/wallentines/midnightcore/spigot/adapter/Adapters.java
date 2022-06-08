package org.wallentines.midnightcore.spigot.adapter;

import org.bukkit.Bukkit;
import org.wallentines.midnightcore.api.MidnightCoreAPI;

public class Adapters {


    public static final String API_VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".",",").split(",")[3];

    public static void findAdapter() {

        SpigotAdapter adapter;
        switch(API_VERSION) {
            case "v1_18_R2":
                adapter = new org.wallentines.midnightcore.spigot.adapter.Adapter_v1_18_R2();
                break;
            case "v1_18_R1":
                adapter = new org.wallentines.midnightcore.spigot.adapter.Adapter_v1_18_R1();
                break;
            default:
                MidnightCoreAPI.getLogger().warn("Version unsupported! Features like RGB text and Skins may not work properly!");
                adapter = new org.wallentines.midnightcore.spigot.adapter.GenericAdapter();
        }

        AdapterManager.setAdapter(adapter);
    }

}
