package org.wallentines.mcore.text;

import org.wallentines.mcore.GameVersion;
import org.wallentines.mdcfg.serializer.InlineSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.math.Color;

public final class TextColor {

    private static final String[] LEGACY_COLOR_NAMES = { "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white"};
    private static final String[] DYE_COLOR_NAMES = { "black", "blue", "green", "cyan", "red", "purple", "orange", "light_gray", "gray", "blue", "lime", "light_blue", "red", "pink", "yellow", "white"};
    private static final byte[] LEGACY_DATA_VALUES = { 15, 11, 13, 9, 14, 10, 1, 8, 7, 11, 5, 3, 14, 6, 4, 0 };

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
    public static final Color WHITE = Color.fromRGBI(15);

    /**
     * Gets an RGB color value based on the given name of a legacy Minecraft color (i.e. green/gold/light_purple)
     * @param name The name of the color to lookup
     * @return A color corresponding to the given name, or null
     */
    public static Color fromLegacyName(String name) {
        for (int i = 0; i < LEGACY_COLOR_NAMES.length; i++) {
            if (LEGACY_COLOR_NAMES[i].equals(name)) {
                return Color.fromRGBI(i);
            }
        }
        return null;
    }

    /**
     * Gets an RGB color value based on the given dye name of a Minecraft dye name (i.e. line/orange/pink)
     * @param name The name of the dye color to lookup
     * @return A color corresponding to the given name, or null
     */
    public static Color fromDyeColor(String name) {
        for (int i = 0; i < DYE_COLOR_NAMES.length; i++) {
            if (DYE_COLOR_NAMES[i].equals(name)) {
                return Color.fromRGBI(i);
            }
        }
        return null;
    }

    /**
     * Determines the legacy item data/damage value for a given color. (i.e pink_wool == wool:6 in versions pre-1.13)
     * @param color The color to lookup
     * @return The legacy item damage value closest to that color
     */
    public static byte getLegacyDataValue(Color color) {

        return LEGACY_DATA_VALUES[color.toRGBI()];
    }

    /**
     * Parses a String into a color, either from a hex code, or from a legacy color name.
     * @param value The value to parse
     * @return The parsed color
     */
    public static Color parse(String value) {

        if (value.startsWith("#")) return Color.parse(value).getOrThrow();
        return fromLegacyName(value);
    }

    /**
     * Gets the legacy color name from a given color (i.e. green/gold/light_purple)
     * @param color The color to convert
     * @return The legacy color closest to the given color
     */
    public static String toLegacyColor(Color color) {
        return LEGACY_COLOR_NAMES[color.toRGBI()];
    }

    /**
     * Gets the dye color name from a given color (i.e. line/orange/pink)
     * @param color The color to convert
     * @return The dye color closest to the given color
     */
    public static String toDyeColor(Color color) {
        return DYE_COLOR_NAMES[color.toRGBI()];
    }

    /**
     * Serializes the color into either an RGB hex value if the game version supports it, or a legacy color name otherwise
     * @param color The color to serialize
     * @return A color usable in components
     */
    public static String serialize(Color color) {
        return GameVersion.CURRENT_VERSION.get().hasFeature(GameVersion.Feature.RGB_TEXT) ? color.toHex() : toLegacyColor(color);
    }

    public static final Serializer<Color> SERIALIZER = InlineSerializer.of(TextColor::serialize, TextColor::parse);

    private TextColor() { }
}
