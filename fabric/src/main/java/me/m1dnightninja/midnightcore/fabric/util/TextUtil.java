package me.m1dnightninja.midnightcore.fabric.util;

import com.google.gson.JsonParseException;
import me.m1dnightninja.midnightcore.api.math.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;

import java.util.ArrayList;
import java.util.List;

public final class TextUtil {

    public static MutableComponent parse(String s) {

        try {
            return Component.Serializer.fromJson(s);
        } catch (JsonParseException ex) {
            return new TextComponent(s);
        }
    }

    public static String toLegacyText(Component component) {

        StringBuilder out = new StringBuilder();
        Style s = component.getStyle();

        if(s.getColor() != null) {
            Color c = fromTextColor(s.getColor());
            if(c != null) {
                out.append("§").append(Integer.toHexString(c.toRGBI()));
            }
        }

        if(s.isBold()) {
            out.append("§l");
        }

        if(s.isItalic()) {
            out.append("§o");
        }

        if(s.isObfuscated()) {
            out.append("§k");
        }

        if(s.isUnderlined()) {
            out.append("§n");
        }

        if(s.isStrikethrough()) {
            out.append("§m");
        }

        out.append(component.getContents());

        for(Component cmp : component.getSiblings()) {
            out.append(toLegacyText(cmp));
        }

        return out.toString();

    }

    private static Color fromTextColor(TextColor clr) {

        String val = clr.serialize();
        if(val.startsWith("#")) {
            return new Color(val);
        } else {

            ChatFormatting fmt = ChatFormatting.getByName(val);
            if(fmt == null || fmt.getColor() == null) return null;

            return new Color(fmt.getColor());
        }
    }

    public static MutableComponent substringText(Component component, int begin, int end) {

        String contents = component.plainCopy().getContents();
        if(contents.length() < begin) {
            return new TextComponent("");
        } else if(end > contents.length()) {
            return component.copy();
        }

        List<Component> cmps = new ArrayList<>();
        cmps.add(component);
        cmps.addAll(component.getSiblings());

        MutableComponent out = new TextComponent("").setStyle(component.getStyle());

        int index = 0;
        for(Component c : cmps) {
            contents = c.getContents();
            StringBuilder add = new StringBuilder();

            for(int i = 0 ; i < contents.length() ; i++) {
                if(index >= begin && index < end) add.append(contents.charAt(i));
                index++;
            }

            if(!add.isEmpty()) {
                out.append(new TextComponent(add.toString()).setStyle(c.getStyle()));
            }
            if(index > end) break;
        }

        return out;
    }

}
