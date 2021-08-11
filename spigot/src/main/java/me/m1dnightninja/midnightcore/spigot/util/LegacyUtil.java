package me.m1dnightninja.midnightcore.spigot.util;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.math.Color;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Locale;

public final class LegacyUtil {

    private static final byte[] data = { 15, 11, 13, 9, 14, 10, 1, 8, 7, 11, 5, 3, 14, 2, 4, 0 };
    private static final HashMap<String, ItemData> conversions = new HashMap<>();

    public static byte colorToData(Color color) {

        return data[color.toRGBI()];
    }

    @SuppressWarnings("deprecation")
    public static ItemStack fromLegacyMaterial(MIdentifier id) {

        if(id.getNamespace().equals("minecraft")) {

            String s = id.getPath().toLowerCase(Locale.ROOT);

            if(conversions.containsKey(s)) {

                try {

                    ItemData dt = conversions.get(s);

                    Material m = Material.valueOf(dt.id);
                    return new ItemStack(m, 1, (short) 0, dt.data);

                } catch (IllegalArgumentException ex) {
                    // IGNORE
                }
            }

            if (s.contains("_")) {

                String[] parts = s.split("_");
                Color clr = Color.fromDyeColor(parts[0]);

                if (clr != null) {

                    StringBuilder str = new StringBuilder();
                    for (int i = 1; i < parts.length; i++) {
                        if(i > 1) {
                            str.append("_");
                        }
                        str.append(parts[i].toUpperCase(Locale.ROOT));
                    }

                    Material m = Material.matchMaterial(str.toString());
                    if(m != null) return new ItemStack(m, 1, (short) 0, colorToData(clr));
                }
            }
        }

        Material m = Material.matchMaterial(id.toString());
        if(m == null) return null;

        return new ItemStack(m);

    }

    private static class ItemData {

        String id;
        byte data;

        public ItemData(String id, byte data) {
            this.id = id;
            this.data = data;
        }
    }

    static {

        conversions.put("skeleton_skull", new ItemData("SKULL_ITEM", (byte) 0));
        conversions.put("wither_skeleton_skull", new ItemData("SKULL_ITEM", (byte) 1));
        conversions.put("zombie_head", new ItemData("SKULL_ITEM", (byte) 2));
        conversions.put("player_head", new ItemData("SKULL_ITEM", (byte) 3));
        conversions.put("creeper_head", new ItemData("SKULL_ITEM", (byte) 4));

    }


}
