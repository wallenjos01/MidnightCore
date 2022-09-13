package org.wallentines.midnightcore.spigot.adapter;

import org.bukkit.Bukkit;
import org.wallentines.midnightcore.api.MidnightCoreAPI;

public class Adapters {

    private static final String API_VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".",",").split(",")[3];

    public static void findAdapter() {

        SpigotAdapter adapter;
        switch(API_VERSION) {
            case "v1_19_R1":
                adapter = new Adapter_v1_19_R1();
                break;
            case "v1_18_R2":
                adapter = new Adapter_v1_18_R2();
                break;
            case "v1_18_R1":
                adapter = new Adapter_v1_18_R1();
                break;
            case "v1_17_R1":
                adapter = new Adapter_v1_17_R1();
                break;
            case "v1_16_R3":
                adapter = new Adapter_v1_16_R3();
                break;
            case "v1_16_R2":
                adapter = new Adapter_v1_16_R2();
                break;
            case "v1_16_R1":
                adapter = new Adapter_v1_16_R1();
                break;
            case "v1_15_R1":
                adapter = new Adapter_v1_15_R1();
                break;
            case "v1_14_R1":
                adapter = new Adapter_v1_14_R1();
                break;
            case "v1_13_R2":
                adapter = new Adapter_v1_13_R2();
                break;
            case "v1_12_R1":
                adapter = new Adapter_v1_12_R1();
                break;
            case "v1_11_R1":
                adapter = new Adapter_v1_11_R1();
                break;
            case "v1_10_R1":
                adapter = new Adapter_v1_10_R1();
                break;
            case "v1_9_R2":
                adapter = new Adapter_v1_9_R2();
                break;
            case "v1_9_R1":
                adapter = new Adapter_v1_9_R1();
                break;
            case "v1_8_R3":
                adapter = new Adapter_v1_8_R3();
                break;
            case "v1_8_R2":
                adapter = new Adapter_v1_8_R2();
                break;
            case "v1_8_R1":
                adapter = new Adapter_v1_8_R1();
                break;
            default:
                MidnightCoreAPI.getLogger().warn("Version unsupported! Features like RGB text and Skins may not work properly!");
                adapter = org.wallentines.midnightcore.spigot.adapter.GenericAdapter.INSTANCE;
        }

        if(!adapter.init()) {
            MidnightCoreAPI.getLogger().warn("An error occurred while initializing an adapter! Features like RGB text and Skins may not work properly!");
            adapter = org.wallentines.midnightcore.spigot.adapter.GenericAdapter.INSTANCE;
        }

        AdapterManager.setAdapter(adapter);
    }

}
