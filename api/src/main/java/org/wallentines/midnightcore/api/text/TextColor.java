package org.wallentines.midnightcore.api.text;

import org.wallentines.mdcfg.serializer.InlineSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.math.Color;

public class TextColor {

    private static final String[] LEGACY_COLOR_NAMES = {"black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white"};
    private static final String[] DYE_COLOR_NAMES = {"black", "blue", "green", "cyan", "red", "purple", "orange", "light_gray", "gray", "blue", "lime", "light_blue", "red", "pink", "yellow", "white"};

    public static final Color BLACK = Color.fromRGBI(0);
    public static final Color DARK_BLUE = Color.fromRGBI(1);
    public static final Color DARK_GREEN = Color.fromRGBI(2);
    public static final Color DARK_AQUA = Color.fromRGBI(3);
    public static final Color DARK_RED = Color.fromRGBI(4);
    public static final Color DARK_PURPLE = Color.fromRGBI(5);
    public static final Color GOLD = Color.fromRGBI(6);
    public static final Color GRAY = Color.fromRGBI(7);
    public static final Color DARK_GRAY = Color.fromRGBI(8);
    public static final Color BLUE = Color.fromRGBI(9);
    public static final Color GREEN = Color.fromRGBI(10);
    public static final Color AQUA = Color.fromRGBI(11);
    public static final Color RED = Color.fromRGBI(12);
    public static final Color LIGHT_PURPLE = Color.fromRGBI(13);
    public static final Color YELLOW = Color.fromRGBI(14);


    public static Color fromLegacyName(String s) {
        for (int i = 0; i < LEGACY_COLOR_NAMES.length; i++) {
            if (LEGACY_COLOR_NAMES[i].equals(s)) {
                return Color.fromRGBI(i);
            }
        }
        return null;
    }

    public static Color fromDyeColor(String s) {
        for (int i = 0; i < DYE_COLOR_NAMES.length; i++) {
            if (DYE_COLOR_NAMES[i].equals(s)) {
                return Color.fromRGBI(i);
            }
        }
        return null;
    }

    public static Color parse(String s) {

        if (s.startsWith("#")) return new Color(s);
        return fromLegacyName(s);
    }

    public static String toDyeColor(Color color) {
        return DYE_COLOR_NAMES[color.toRGBI()];
    }

    public static String toLegacyColor(Color color) {
        return LEGACY_COLOR_NAMES[color.toRGBI()];
    }

    public static Color fromRGBI(int i) {
        return Color.fromRGBI(i);
    }

    public static final Serializer<Color> SERIALIZER = new InlineSerializer<>() {
        @Override
        public Color readString(String str) {
            return parse(str);
        }

        @Override
        public String writeString(Color value) {

            MidnightCoreAPI instance = MidnightCoreAPI.getInstance();
            if(instance == null || instance.getGameVersion().getMinorVersion() >= 16) {
                return value.toHex();
            }

            return toLegacyColor(value);
        }
    };
}
