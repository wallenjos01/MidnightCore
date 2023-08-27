package org.wallentines.mcore;

import org.bukkit.Material;
import org.wallentines.mcore.text.TextColor;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashMap;
import java.util.Locale;

public class LegacyUtil {


    private static final byte[] DATA = { 15, 11, 13, 9, 14, 10, 1, 8, 7, 11, 5, 3, 14, 6, 4, 0 };
    private static final HashMap<String, ItemData> STATIC_CONVERSIONS = new HashMap<>();

    public static byte colorToData(Color color) {

        return DATA[color.toRGBI()];
    }

    @SuppressWarnings("deprecation")
    public static ItemData fromLegacyMaterial(Identifier id) {

        String s = id.getPath().toLowerCase(Locale.ENGLISH);

        if(STATIC_CONVERSIONS.containsKey(s)) {

            try {

                ItemData dt = STATIC_CONVERSIONS.get(s);

                Material m = Material.valueOf(dt.id);
                return new ItemData(m.name(), dt.data);

            } catch (IllegalArgumentException ex) {
                // IGNORE
            }
        }

        if (s.contains("_")) {

            String[] parts = s.split("_");
            Color clr = TextColor.fromDyeColor(parts[0]);

            if (clr != null) {

                StringBuilder str = new StringBuilder();
                for (int i = 1; i < parts.length; i++) {
                    if(i > 1) {
                        str.append("_");
                    }
                    str.append(parts[i].toUpperCase(Locale.ENGLISH));
                }

                Material m = Material.matchMaterial(str.toString());
                if(m != null) return new ItemData(m.name(), colorToData(clr));
            }
        }

        Material m = Material.matchMaterial(s);

        if(m == null) {
            return null;
        }

        return new ItemData(m.name(), (byte) -1);

    }

    public static class ItemData {

        String id;
        byte data;

        public ItemData(String id, byte data) {
            this.id = id;
            this.data = data;
        }
    }

    static {

        STATIC_CONVERSIONS.put("skeleton_skull", new ItemData("SKULL_ITEM", (byte) 0));
        STATIC_CONVERSIONS.put("wither_skeleton_skull", new ItemData("SKULL_ITEM", (byte) 1));
        STATIC_CONVERSIONS.put("zombie_head", new ItemData("SKULL_ITEM", (byte) 2));
        STATIC_CONVERSIONS.put("player_head", new ItemData("SKULL_ITEM", (byte) 3));
        STATIC_CONVERSIONS.put("creeper_head", new ItemData("SKULL_ITEM", (byte) 4));

    }


}
