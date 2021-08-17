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

    private Boolean reset;

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

    public MStyle withReset(Boolean b) {
        this.reset = b;
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
        return new MStyle().withColor(color).withFont(font).withBold(bold).withItalic(italic).withUnderline(underline).withStrikethrough(strikethrough).withObfuscated(obfuscated).withReset(reset);
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
        if(other.reset != null) {
            reset = other.reset;
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
            base.append("\"color\":\"").append(color).append("\"");
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
            base.append("\"font\":\"").append(font).append("\"");
        }

        return base.toString();

    }

    public String toLegacyText(Character colorChar, Character hexChar) {

        boolean hexSupport = hexChar != null;

        StringBuilder out = new StringBuilder();
        if(color != null) {
            out.append(hexSupport ? hexChar + color.toPlainHex() : colorChar + Integer.toHexString(color.toRGBI()));
        }
        if(bold != null && bold) {
            out.append(colorChar).append("l");
        }
        if(italic != null && italic) {
            out.append(colorChar).append("o");
        }
        if(underline != null && underline) {
            out.append(colorChar).append("n");
        }
        if(strikethrough != null && strikethrough) {
            out.append(colorChar).append("m");
        }
        if(obfuscated != null && obfuscated) {
            out.append(colorChar).append("k");
        }
        if(reset != null && reset) {
            out.append(colorChar).append("r");
        }

        return out.toString();

    }

    public static final MStyle ITEM_BASE = new MStyle().withItalic(Boolean.FALSE);
    public static final MStyle LEGACY_RESET = new MStyle().withReset(Boolean.TRUE);

}
