package org.wallentines.mcore.util;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.wallentines.mcore.text.ClickEvent;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.HoverEvent;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

public class ConversionUtil {

    /**
     * Converts a Minecraft {@link net.minecraft.resources.ResourceLocation ResourceLocation} to an {@link org.wallentines.midnightlib.registry.Identifier Identifier}
     * @param location The ResourceLocation to convert
     * @return A new Identifier
     */
    public static Identifier toIdentifier(ResourceLocation location) {

        return new Identifier(location.getNamespace(), location.getPath());
    }

    /**
     * Converts an Identifier to a Minecraft ResourceLocation
     * @param location The Identifier to convert
     * @return A new ResourceLocation
     */
    public static ResourceLocation toResourceLocation(Identifier location) {

        return new ResourceLocation(location.getNamespace(), location.getPath());
    }

    /**
     * Converts a MidnightCore {@link org.wallentines.mcore.text.HoverEvent HoverEvent} to a Minecraft {@link net.minecraft.network.chat.HoverEvent HoverEvent}
     * @param event The HoverEvent to convert
     * @return A new Minecraft HoverEvent
     */
    public static net.minecraft.network.chat.HoverEvent toMCHoverEvent(HoverEvent event) {

        net.minecraft.network.chat.HoverEvent.Action<?> act = net.minecraft.network.chat.HoverEvent.Action.getByName(event.getAction().getId());
        if(act == null) throw new IllegalArgumentException("Don't know how to convert HoverEvent of type " + event.getAction().id + " to a Minecraft Hover event!");

        JsonObject obj = ConfigContext.INSTANCE.convert(GsonContext.INSTANCE, event.getContents()).getAsJsonObject();
        return act.deserialize(obj);
    }

    /**
     * Converts a Minecraft {@link net.minecraft.network.chat.HoverEvent HoverEvent} to a MidnightCore {@link org.wallentines.mcore.text.HoverEvent HoverEvent}
     * @param event The HoverEvent to convert
     * @return A new MidnightCore HoverEvent
     */
    public static HoverEvent toHoverEvent(net.minecraft.network.chat.HoverEvent event) {

        return new HoverEvent(
                HoverEvent.Action.byId(event.getAction().getName()),
                GsonContext.INSTANCE.convert(ConfigContext.INSTANCE, event.getAction().serializeArg(event.getValue(event.getAction()))).asSection()
        );
    }

    /**
     * Converts a MidnightCore {@link org.wallentines.mcore.text.ClickEvent ClickEvent} to a Minecraft {@link net.minecraft.network.chat.ClickEvent ClickEvent}
     * @param event The ClickEvent to convert
     * @return A new Minecraft ClickEvent
     */
    public static net.minecraft.network.chat.ClickEvent toMCClickEvent(ClickEvent event) {
        return new net.minecraft.network.chat.ClickEvent(
                net.minecraft.network.chat.ClickEvent.Action.getByName(event.getAction().getId()),
                event.getValue()
        );
    }

    /**
     * Converts a Minecraft {@link net.minecraft.network.chat.ClickEvent ClickEvent} to a MidnightCore {@link org.wallentines.mcore.text.ClickEvent ClickEvent}
     * @param event The ClickEvent to convert
     * @return A new MidnightCore ClickEvent
     */
    public static ClickEvent toClickEvent(net.minecraft.network.chat.ClickEvent event) {

        return new ClickEvent(
                ClickEvent.Action.byId(event.getAction().getName()),
                event.getValue()
        );

    }

    /**
     * Converts an RGB {@link org.wallentines.midnightlib.math.Color Color} to a Minecraft {@link net.minecraft.network.chat.TextColor TextColor}
     * @param color The Color to convert
     * @return A new TextColor
     */
    public static TextColor toTextColor(Color color) {

        return TextColor.fromRgb(color.toDecimal());
    }

    /**
     * Converts a Minecraft {@link net.minecraft.network.chat.TextColor TextColor} to an RGB {@link org.wallentines.midnightlib.math.Color Color}
     * @param color The TextColor to convert
     * @return A new Color
     */
    public static Color toColor(TextColor color) {
        return new Color(color.getValue());
    }

    public static Style getStyle(Component component) {

        return Style.EMPTY
                .withBold(component.bold)
                .withItalic(component.italic)
                .withUnderlined(component.underlined)
                .withStrikethrough(component.strikethrough)
                .withObfuscated(component.obfuscated)
                .withInsertion(component.insertion)
                .withFont(component.font == null ? null : ConversionUtil.toResourceLocation(component.font))
                .withHoverEvent(component.hoverEvent == null ? null : ConversionUtil.toMCHoverEvent(component.hoverEvent))
                .withClickEvent(component.clickEvent == null ? null : ConversionUtil.toMCClickEvent(component.clickEvent))
                .withColor(component.color == null ? null : ConversionUtil.toTextColor(component.color));
    }

}
