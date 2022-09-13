package org.wallentines.midnightcore.api.text;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

public class MStyle {

    private TextColor color;
    private Boolean bold;
    private Boolean italic;
    private Boolean underlined;
    private Boolean strikethrough;
    private Boolean obfuscated;
    private Identifier font;

    public TextColor getColor() {
        return color;
    }

    public Boolean getBold() {
        return bold;
    }

    public Boolean getItalic() {
        return italic;
    }

    public Boolean getUnderlined() {
        return underlined;
    }

    public Boolean getStrikethrough() {
        return strikethrough;
    }

    public Boolean getObfuscated() {
        return obfuscated;
    }

    public Identifier getFont() {
        return font;
    }

    public MStyle withColor(TextColor value) { this.color = value; return this; }

    public MStyle withColor(Color value) { this.color = new TextColor(value); return this; }

    public MStyle withBold(Boolean value) { this.bold = value; return this; }

    public MStyle withItalic(Boolean value) { this.italic = value; return this; }

    public MStyle withUnderlined(Boolean value) { this.underlined = value; return this; }

    public MStyle withStrikethrough(Boolean value) { this.strikethrough = value; return this; }

    public MStyle withObfuscated(Boolean value) { this.obfuscated = value; return this; }

    public MStyle withFont(Identifier value) { this.font = value; return this; }

    public MStyle fillFrom(MStyle other) {
        
        if(color == null) color = other.color;
        if(bold == null) bold = other.bold;
        if(italic == null) italic = other.italic;
        if(underlined == null) underlined = other.underlined;
        if(strikethrough == null) strikethrough = other.strikethrough;
        if(obfuscated == null) obfuscated = other.obfuscated;
        if(font == null) font = other.font;

        return this;
    }

    public boolean hasFormatting() {
        return bold != null || italic != null || underlined != null || strikethrough != null || obfuscated != null || font != null;
    }

    public MStyle reset() {

        color = null;
        bold = null;
        italic = null;
        underlined = null;
        strikethrough = null;
        obfuscated = null;
        font = null;

        return this;
    }

    public MStyle copy() {

        return new MStyle().withColor(color).withBold(bold).withItalic(italic).withUnderlined(underlined).withStrikethrough(strikethrough).withObfuscated(obfuscated).withFont(font);
    }

    public String toLegacyStyle(Character colorChar, Character hexChar) {

        boolean hexSupport = hexChar != null;
        StringBuilder out = new StringBuilder();

        if(color != null)                          out.append(hexSupport ?
                                                        hexChar + color.toPlainHex() :
                                                        colorChar + Integer.toHexString(color.toRGBI()));

        if(bold != null && bold)                   out.append(colorChar).append("l");
        if(italic  != null && italic)              out.append(colorChar).append("o");
        if(underlined != null && underlined)       out.append(colorChar).append("n");
        if(strikethrough != null && strikethrough) out.append(colorChar).append("m");
        if(obfuscated != null && obfuscated)       out.append(colorChar).append("k");

        return out.toString();
    }

    public static final ConfigSerializer<MStyle> SERIALIZER = new ConfigSerializer<>() {
        @Override
        public MStyle deserialize(ConfigSection section) {

            MStyle out = new MStyle();
            if(section.has("color")) out.color = section.get("color", TextColor.class);
            if(section.has("bold", Boolean.class)) out.bold = section.getBoolean("bold");
            if(section.has("italic", Boolean.class)) out.italic = section.getBoolean("italic");
            if(section.has("underlined", Boolean.class)) out.underlined = section.getBoolean("underlined");
            if(section.has("strikethrough", Boolean.class)) out.strikethrough = section.getBoolean("strikethrough");
            if(section.has("obfuscated", Boolean.class)) out.obfuscated = section.getBoolean("obfuscated");
            if(section.has("font", Identifier.class)) out.font = section.get("font", Identifier.class);

            return out;
        }

        @Override
        public ConfigSection serialize(MStyle object) {

            ConfigSection out = new ConfigSection();

            if(MidnightCoreAPI.getInstance() == null || MidnightCoreAPI.getInstance().getGameVersion().getMinorVersion() >= 16) {
                out.set("color", object.color);
            } else if(object.color != null) {
                out.set("color", TextColor.LEGACY_SERIALIZER.serialize(object.color));
            }
            out.set("bold", object.bold);
            out.set("italic", object.italic);
            out.set("underlined", object.underlined);
            out.set("strikethrough", object.strikethrough);
            out.set("obfuscated", object.obfuscated);
            out.set("font", object.font);

            return out;
        }
    };

    public static final MStyle ITEM_NAME_BASE = new MStyle().withItalic(false);
    public static final MStyle ITEM_LORE_BASE = new MStyle().withColor(Color.WHITE).withItalic(false);

}
