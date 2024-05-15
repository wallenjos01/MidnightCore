package org.wallentines.mcore.util;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.wallentines.mcore.ConfiguringPlayer;
import org.wallentines.mcore.Entity;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.text.*;
import org.wallentines.mdcfg.serializer.GsonContext;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Optional;
import java.util.stream.Stream;

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

            ItemStack is = ConversionUtil.validate((org.wallentines.mcore.ItemStack) event.getValue());
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
            return net.minecraft.network.chat.ClickEvent.CODEC.decode(JsonOps.INSTANCE, obj).getOrThrow().getFirst();
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

        Content contents = toContent(other.getContents());
        Component out = new Component(contents);

        Style style = other.getStyle();
        if(!style.isEmpty()) {

            if(style.isBold()) out = out.withBold(true);
            if(style.isItalic()) out = out.withItalic(true);
            if(style.isUnderlined()) out = out.withUnderlined(true);
            if(style.isStrikethrough()) out = out.withStrikethrough(true);
            if(style.isObfuscated()) out = out.withObfuscated(true);

            if(style.getFont() != Style.DEFAULT_FONT) out = out.withFont(ConversionUtil.toIdentifier(style.getFont()));

            out = out.withInsertion(style.getInsertion());
            if(style.getColor() != null) out = out.withColor(ConversionUtil.toColor(style.getColor()));
            if(style.getHoverEvent() != null) out = out.withHoverEvent(ConversionUtil.toHoverEvent(style.getHoverEvent()));
            if(style.getClickEvent() != null) out = out.withClickEvent(ConversionUtil.toClickEvent(style.getClickEvent()));
        }

        for(net.minecraft.network.chat.Component child : other.getSiblings()) {
            out = out.addChild(toComponent(child));
        }

        return out;
    }

    public static ComponentContents toContents(Content content) {

        switch (content.getType()) {
            case TEXT:
                return new PlainTextContents.LiteralContents(((Content.Text) content).text);
            case TRANSLATE: {
                Content.Translate md = (Content.Translate) content;
                return new TranslatableContents(
                        md.key,
                        md.fallback,
                        md.with == null ? null : md.with.stream().map(WrappedComponent::new).toArray());
            }
            case KEYBIND:
                return new KeybindContents(((Content.Keybind) content).key);
            case SCORE: {
                Content.Score md = (Content.Score) content;
                return new ScoreContents(md.name, md.objective);
            }
            case SELECTOR: {
                Content.Selector md = (Content.Selector) content;
                return new SelectorContents(
                        md.value,
                        Optional.ofNullable(md.separator == null ? null : new WrappedComponent(md.separator)));
            }
            case NBT: {
                Content.NBT md = (Content.NBT) content;
                DataSource source = switch (md.type) {
                    case BLOCK -> new BlockDataSource(md.data);
                    case ENTITY -> new EntityDataSource(md.data);
                    default -> new StorageDataSource(new ResourceLocation(md.data));
                };
                return new NbtContents(
                        md.path,
                        md.interpret,
                        Optional.ofNullable(md.separator == null ? null : new WrappedComponent(md.separator)),
                        source
                );
            }
            default:
                throw new IllegalArgumentException("Don't know how to turn " + content + " into a Minecraft content!");
        }
    }

    public static Content toContent(ComponentContents contents) {

        if(contents instanceof PlainTextContents pt) {
            return new Content.Text(pt.text());
        }
        else if(contents instanceof TranslatableContents mc) {
            return new Content.Translate(
                    mc.getKey(),
                    mc.getFallback(),
                    mc.getArgs().length == 0 ? null : Stream.of(mc.getArgs()).map(obj -> toComponent((net.minecraft.network.chat.Component) obj)).toList());
        }
        else if(contents instanceof KeybindContents mc) {
            return new Content.Keybind(mc.getName());
        }
        else if(contents instanceof ScoreContents mc) {
            return new Content.Score(mc.getName(), mc.getObjective(), null);
        }
        else if(contents instanceof SelectorContents mc) {
            return new Content.Selector(
                    mc.getPattern(),
                    mc.getSeparator().map(ConversionUtil::toComponent).orElse(null));
        }
        else if(contents instanceof NbtContents mc) {
            String pattern;
            Content.NBT.DataSourceType type;
            if(mc.getDataSource() instanceof BlockDataSource) {
                pattern = ((BlockDataSource) mc.getDataSource()).posPattern();
                type = Content.NBT.DataSourceType.BLOCK;
            }
            else if(mc.getDataSource() instanceof EntityDataSource) {
                pattern = ((EntityDataSource) mc.getDataSource()).selectorPattern();
                type = Content.NBT.DataSourceType.ENTITY;
            }
            else {
                pattern = ((StorageDataSource) mc.getDataSource()).id().toString();
                type = Content.NBT.DataSourceType.STORAGE;
            }
            return new Content.NBT(mc.getNbtPath(), mc.isInterpreting(), mc.getSeparator().map(ConversionUtil::toComponent).orElse(null), type, pattern);
        }

        throw new IllegalArgumentException("Don't know how to convert " + contents + " into a MidnightCore content!");
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
     * Validates that the given configuring player is actually a Minecraft ServerConfigurationPacketListenerImpl
     * @param player The configuring player to check
     * @return The player casted to a ServerConfigurationPacketListenerImpl
     */
    public static ServerConfigurationPacketListenerImpl validate(ConfiguringPlayer player) {

        if(!(player instanceof ServerConfigurationPacketListenerImpl cpl)) {
            throw new IllegalArgumentException("Attempt to access non-Minecraft Player!");
        }
        return cpl;
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
