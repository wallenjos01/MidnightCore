package me.m1dnightninja.midnightcore.api.text;

import me.m1dnightninja.midnightcore.api.math.Color;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;

public class MStyle {

    private Color color;
    private Boolean bold;
    private Boolean italic;
    private Boolean underline;
    private Boolean strikethrough;
    private Boolean obfuscated;
    private MIdentifier font;

    public MStyle() { }

    public MStyle withColor(Color color) {
        this.color = color;
        return this;
    }

    public MStyle withBold(Boolean b) {
        this.bold = b;
        return this;
    }

    public MStyle withItalic(Boolean b) {
        this.italic = b;
        return this;
    }

    public MStyle withUnderline(Boolean b) {
        this.underline = b;
        return this;
    }

    public MStyle withStrikethrough(Boolean b) {
        this.strikethrough = b;
        return this;
    }

    public MStyle withObfuscated(Boolean b) {
        this.obfuscated = b;
        return this;
    }

    public MStyle withFont(MIdentifier font) {
        this.font = font;
        return this;
    }

    public Color getColor() {
        return color;
    }

    public Boolean isBold() {
        return bold;
    }

    public Boolean isItalic() {
        return italic;
    }

    public Boolean isUnderlined() {
        return underline;
    }

    public Boolean isStrikethrough() {
        return strikethrough;
    }

    public Boolean isObfuscated() {
        return obfuscated;
    }

    public MIdentifier getFont() {
        return font;
    }

    public MStyle copy() {
        return new MStyle().withColor(color).withFont(font).withBold(bold).withItalic(italic).withUnderline(underline).withStrikethrough(strikethrough).withObfuscated(obfuscated);
    }

    public MStyle fill(MStyle other) {

        if(other.color != null) {
            color = other.color;
        }
        if(other.bold != null) {
            bold = other.bold;
        }
        if(other.italic != null) {
            italic = other.italic;
        }
        if(other.underline != null) {
            underline = other.underline;
        }
        if(other.strikethrough != null) {
            strikethrough = other.strikethrough;
        }
        if(other.obfuscated != null) {
            obfuscated = other.obfuscated;
        }
        if(other.font != null) {
            font = other.font;
        }

        return this;
    }

    public MStyle merge(MStyle other) {

        MStyle out = copy();
        out.fill(other);

        return out;
    }

    @Override
    public String toString() {

        StringBuilder base = new StringBuilder();

        if(color != null) {
            base.append("\"color\":\"").append(color.toString()).append("\"");
        }
        if(bold != null) {
            base.append("\"bold\":").append(bold);
        }
        if(italic != null) {
            base.append("\"italic\":").append(italic);
        }
        if(underline != null) {
            base.append("\"underlined\":").append(underline);
        }
        if(strikethrough != null) {
            base.append("\"strikethrough\":").append(strikethrough);
        }
        if(obfuscated != null) {
            base.append("\"obfuscated\":").append(obfuscated);
        }
        if(font != null) {
            base.append("\"font\":\"").append(font.toString()).append("\"");
        }

        return base.toString();

    }

    public String toLegacyText(boolean hexSupport) {

        StringBuilder out = new StringBuilder();
        if(color != null) {
            out.append(hexSupport ? color.toHex() : "§" + Integer.toHexString(color.toRGBI()));
        }
        if(bold != null && bold) {
            out.append("§l");
        }
        if(italic != null && italic) {
            out.append("§o");
        }
        if(underline != null && underline) {
            out.append("§m");
        }
        if(strikethrough != null && strikethrough) {
            out.append("§n");
        }
        if(obfuscated != null && obfuscated) {
            out.append("§k");
        }

        return out.toString();

    }

    public static final MStyle ITEM_BASE = new MStyle().withItalic(Boolean.FALSE);

}
