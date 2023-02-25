package org.wallentines.midnightcore.api.text;

import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

public class MStyle {

    private Color color;
    private Boolean bold;
    private Boolean italic;
    private Boolean underlined;
    private Boolean strikethrough;
    private Boolean obfuscated;
    private Identifier font;

    public MStyle() {
    }

    public MStyle(Color color, Boolean bold, Boolean italic, Boolean underlined, Boolean strikethrough, Boolean obfuscated, Identifier font) {
        this.color = color;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        this.obfuscated = obfuscated;
        this.font = font;
    }

    public Color getColor() {
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

    public MStyle withColor(Color value) { this.color = value; return this; }

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


    public static final Serializer<MStyle> SERIALIZER = ObjectSerializer.create(
            TextColor.SERIALIZER.entry("color", MStyle::getColor).optional(),
            Serializer.BOOLEAN.entry("bold", MStyle::getBold).optional(),
            Serializer.BOOLEAN.entry("italic", MStyle::getItalic).optional(),
            Serializer.BOOLEAN.entry("underlined", MStyle::getUnderlined).optional(),
            Serializer.BOOLEAN.entry("strikethrough", MStyle::getStrikethrough).optional(),
            Serializer.BOOLEAN.entry("obfuscated", MStyle::getObfuscated).optional(),
            Identifier.serializer("minecraft").entry("font", MStyle::getFont).optional(),
            MStyle::new
    );


    public static final MStyle ITEM_NAME_BASE = new MStyle().withItalic(false);
    public static final MStyle ITEM_LORE_BASE = new MStyle().withColor(Color.WHITE).withItalic(false);

}
