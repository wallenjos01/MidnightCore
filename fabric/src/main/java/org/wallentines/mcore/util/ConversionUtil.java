package org.wallentines.mcore.util;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.wallentines.mcore.*;
import org.wallentines.mcore.text.*;
import org.wallentines.mdcfg.serializer.GsonContext;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Optional;

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
    public static net.minecraft.network.chat.HoverEvent toMCHoverEvent(HoverEvent<?> event) {

        if(event.getType() == HoverEvent.Type.SHOW_TEXT) {
            net.minecraft.network.chat.Component out = new WrappedComponent((Component) event.getValue());
            return new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, out);
        }

        if(event.getType() == HoverEvent.Type.SHOW_ITEM) {
            HoverEvent.ItemInfo info = (HoverEvent.ItemInfo) event.getValue();

            ItemStack is;
            if(info.getItem() != null) {
                is = validate(info.getItem());

            } else {

                Item it = RegistryUtil.registryOrThrow(Registries.ITEM).getOrThrow(ResourceKey.create(Registries.ITEM, toResourceLocation(info.id)));
                is = new ItemStack(it, info.count);
                if (info.getTag() != null) {
                    try {
                        is.setTag(TagParser.parseTag(info.getTag()));
                    } catch (CommandSyntaxException ex) {
                        throw new IllegalStateException("Unable to convert item tag!", ex);
                    }
                }
            }

            return new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_ITEM, new net.minecraft.network.chat.HoverEvent.ItemStackInfo(is));
        }

        if(event.getType() == HoverEvent.Type.SHOW_ENTITY) {
            HoverEvent.EntityInfo info = (HoverEvent.EntityInfo) event.getValue();
            net.minecraft.network.chat.HoverEvent.EntityTooltipInfo out = new net.minecraft.network.chat.HoverEvent.EntityTooltipInfo(
                    BuiltInRegistries.ENTITY_TYPE.get(toResourceLocation(info.type)),
                    info.uuid,
                    Optional.ofNullable(info.name).map(WrappedComponent::new)
            );
            return new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_ENTITY, out);
        }

        if(event.getType() == HoverEvent.Type.SHOW_ACHIEVEMENT) {

            throw new IllegalArgumentException("show_achievement hover events are not supported in this version!");
        }

        throw new IllegalArgumentException("Don't know how to convert HoverEvent of type " + event.getTypeId() + " to a Minecraft Hover event!");
    }

    /**
     * Converts a Minecraft {@link net.minecraft.network.chat.HoverEvent HoverEvent} to a MidnightCore {@link org.wallentines.mcore.text.HoverEvent HoverEvent}
     * @param event The HoverEvent to convert
     * @return A new MidnightCore HoverEvent
     */
    public static HoverEvent<?> toHoverEvent(net.minecraft.network.chat.HoverEvent event) {

        net.minecraft.network.chat.HoverEvent.Action<?> act = event.getAction();
        if(act == net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT) {
            net.minecraft.network.chat.Component txt = (net.minecraft.network.chat.Component) event.getValue(act);
            return HoverEvent.create(toComponent(txt));
        }

        if(act == net.minecraft.network.chat.HoverEvent.Action.SHOW_ITEM) {
            net.minecraft.network.chat.HoverEvent.ItemStackInfo is = (net.minecraft.network.chat.HoverEvent.ItemStackInfo) event.getValue(act);
            return HoverEvent.forItem((org.wallentines.mcore.ItemStack) (Object) is.getItemStack());
        }

        if(act == net.minecraft.network.chat.HoverEvent.Action.SHOW_ENTITY) {
            net.minecraft.network.chat.HoverEvent.EntityTooltipInfo ent = (net.minecraft.network.chat.HoverEvent.EntityTooltipInfo) event.getValue(act);
            HoverEvent.EntityInfo out = new HoverEvent.EntityInfo(
                    ent.name.map(ConversionUtil::toComponent).orElse(null),
                    toIdentifier(BuiltInRegistries.ENTITY_TYPE.getKey(ent.type)),
                    ent.id
            );
            return HoverEvent.forEntity(out);
        }

        throw new IllegalStateException("Don't know how to turn Minecraft Hover event of type " + event.getAction().getSerializedName() + " to a MidnightCore hover event!");
    }

    /**
     * Converts a MidnightCore {@link org.wallentines.mcore.text.ClickEvent ClickEvent} to a Minecraft {@link net.minecraft.network.chat.ClickEvent ClickEvent}
     * @param event The ClickEvent to convert
     * @return A new Minecraft ClickEvent
     */
    public static net.minecraft.network.chat.ClickEvent toMCClickEvent(ClickEvent event) {

        JsonObject obj = ClickEvent.SERIALIZER.serialize(GsonContext.INSTANCE, event).getOrThrow().getAsJsonObject();

        try {
            return net.minecraft.network.chat.ClickEvent.CODEC.decode(JsonOps.INSTANCE, obj).getOrThrow(false, MidnightCoreAPI.LOGGER::error).getFirst();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Don't know how to convert ClickEvent of type " + event.getAction().id + " to a Minecraft Click event!", ex);
        }
    }

    /**
     * Converts a Minecraft {@link net.minecraft.network.chat.ClickEvent ClickEvent} to a MidnightCore {@link org.wallentines.mcore.text.ClickEvent ClickEvent}
     * @param event The ClickEvent to convert
     * @return A new MidnightCore ClickEvent
     */
    public static ClickEvent toClickEvent(net.minecraft.network.chat.ClickEvent event) {

        return new ClickEvent(
                ClickEvent.Action.byId(event.getAction().getSerializedName()),
                event.getValue()
        );
    }

    public static Component toComponent(net.minecraft.network.chat.Component other) {
        return ModernSerializer.INSTANCE
                .deserialize(
                        NBTContext.INSTANCE,
                        ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, other)
                                .getOrThrow(false, MidnightCoreAPI.LOGGER::error),
                        GameVersion.CURRENT_VERSION.get()
                ).getOrThrow();
    }

    public static EquipmentSlot toMCEquipmentSlot(Entity.EquipmentSlot slot) {
        return switch(slot) {
            case MAINHAND -> net.minecraft.world.entity.EquipmentSlot.MAINHAND;
            case OFFHAND -> net.minecraft.world.entity.EquipmentSlot.OFFHAND;
            case FEET -> net.minecraft.world.entity.EquipmentSlot.FEET;
            case LEGS -> net.minecraft.world.entity.EquipmentSlot.LEGS;
            case CHEST -> net.minecraft.world.entity.EquipmentSlot.CHEST;
            case HEAD -> net.minecraft.world.entity.EquipmentSlot.HEAD;
        };
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

    /**
     * Creates a Minecraft Style from a MidnightCore component
     * @param component The component to read
     * @return A new Style
     */
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

    /**
     * Validates that the given player is actually a Minecraft ServerPlayer
     * @param player The player to check
     * @return The player casted to a ServerPlayer
     */
    public static ServerPlayer validate(Player player) {

        if(!(player instanceof ServerPlayer spl)) {
            throw new IllegalArgumentException("Attempt to access non-Minecraft Player!");
        }
        return spl;
    }

    /**
     * Validates that the given ItemStack is actually a Minecraft ItemStack
     * @param is The ItemStack to check
     * @return The ItemStack casted to a Minecraft ItemStack
     */
    public static ItemStack validate(org.wallentines.mcore.ItemStack is) {

        if(!((Object) is instanceof ItemStack mis)) {
            throw new IllegalArgumentException("Attempt to access non-Minecraft ItemStack!");
        }
        return mis;
    }

    /**
     * Validates that the given Server is actually a Minecraft Server
     * @param srv The Server to check
     * @return The server casted to a Minecraft Server
     */
    public static MinecraftServer validate(Server srv) {

        if(!(srv instanceof MinecraftServer msv)) {
            throw new IllegalArgumentException("Attempt to access non-Minecraft Server!");
        }
        return msv;
    }

}
