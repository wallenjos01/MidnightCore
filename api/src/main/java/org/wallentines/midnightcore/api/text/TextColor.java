package org.wallentines.midnightcore.api.text;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.config.serialization.InlineSerializer;
import org.wallentines.midnightlib.math.Color;

public class TextColor extends Color {

    private static final String[] LEGACY_COLOR_NAMES = {"black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white"};
    private static final String[] DYE_COLOR_NAMES = {"black", "blue", "green", "cyan", "red", "purple", "orange", "light_gray", "gray", "blue", "lime", "light_blue", "red", "pink", "yellow", "white"};

    public TextColor(String name) {
        super(name);
    }

    public TextColor(Color color) {
        super(color.toDecimal());
    }

    public static TextColor fromLegacyName(String s) {
        for (int i = 0; i < LEGACY_COLOR_NAMES.length; i++) {
            if (LEGACY_COLOR_NAMES[i].equals(s)) {
                return new TextColor(Color.fromRGBI(i));
            }
        }
        return null;
    }

    public static TextColor fromDyeColor(String s) {
        for (int i = 0; i < DYE_COLOR_NAMES.length; i++) {
            if (DYE_COLOR_NAMES[i].equals(s)) {
                return new TextColor(Color.fromRGBI(i));
            }
        }
        return null;
    }

    public static TextColor parse(String s) {

        if (s.startsWith("#")) return new TextColor(s);
        return fromLegacyName(s);
    }

    public String toDyeColor() {
        return DYE_COLOR_NAMES[toRGBI()];
    }

    public String toLegacyColor() {
        return LEGACY_COLOR_NAMES[toRGBI()];
    }

    public static final InlineSerializer<TextColor> SERIALIZER = InlineSerializer.of(Color::toHex, TextColor::parse);
}
