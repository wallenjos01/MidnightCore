package org.wallentines.mcore.text;

import org.wallentines.mcore.GameVersion;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.*;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A {@link ContextSerializer} which serializes components into maps in the modern component format
 */
public class ModernSerializer implements ContextSerializer<Component, GameVersion> {


    public static final ModernSerializer INSTANCE = new ModernSerializer();

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context, Component value, GameVersion version) {

        SerializeResult<O> content = serializeContent(context, value.content, version);
        if(!content.isComplete()) {
            return content;
        }

        O out = content.getOrThrow();

        String color = value.reset != null && value.reset ? "reset" : value.color == null ? null : TextColor.serialize(value.color, version);
        context.set("color", context.toString(color), out);

        context.set("bold", context.toBoolean(value.bold), out);
        context.set("italic", context.toBoolean(value.italic), out);
        context.set("underlined", context.toBoolean(value.underlined), out);
        context.set("strikethrough", context.toBoolean(value.strikethrough), out);
        context.set("obfuscated", context.toBoolean(value.obfuscated), out);

        context.set("font", context.toString(value.font == null ? null : value.font.toString()), out);
        context.set("insertion", context.toString(value.insertion), out);

        if (version.hasFeature(GameVersion.Feature.COMPONENT_SHADOW_COLOR) && value.shadowColor != null) {
            context.set("shadow_color", context.toNumber(value.shadowColor.toDecimal()), out);
        }

        if(value.hoverEvent != null) {
            SerializeResult<O> hoverEvent = HoverEvent.SERIALIZER.serialize(context, value.hoverEvent, version);
            if(!hoverEvent.isComplete()) {
                return SerializeResult.failure("Unable to serialize hover event for component!" + hoverEvent.getError());
            }
            context.set("hoverEvent", hoverEvent.getOrThrow(), out);
        }

        if(value.clickEvent != null) {
            SerializeResult<O> clickEvent = ClickEvent.SERIALIZER.serialize(context, value.clickEvent);
            if(!clickEvent.isComplete()) {
                return SerializeResult.failure("Unable to serialize click event for component!" + clickEvent.getError());
            }
            context.set("clickEvent", clickEvent.getOrThrow(), out);
        }


        // Children
        if(!value.children.isEmpty()) {

            List<O> children = new ArrayList<>();
            for(Component cmp : value.children) {
                SerializeResult<O> result = serialize(context, cmp, version);
                if(!result.isComplete()) {
                    return SerializeResult.failure("Unable to serialize children for component!" + result.getError());
                }
                children.add(result.getOrThrow());
            }

            context.set("extra", context.toList(children), out);

        }

        return SerializeResult.success(out);
    }

    private <O> SerializeResult<O> serializeContent(SerializeContext<O> context, Content content, GameVersion version) {

        ContentSerializer<?> serializer = getSerializer(content.type);
        if(serializer == null) {
            return SerializeResult.failure("Unable to serialize content with type " + content.type + "!");
        }

        ContentSerializer.Context ctx = new ContentSerializer.Context(version, this);
        return serializer.serialize(context, content, ctx);
    }

    @Override
    public <O> SerializeResult<Component> deserialize(SerializeContext<O> context, O value, GameVersion version) {

        if(context.isString(value)) {
            return SerializeResult.success(Component.text(context.asString(value)));
        }
        if(context.isBoolean(value)) {
            return SerializeResult.success(Component.text(String.valueOf(context.asBoolean(value))));
        }
        if(context.isNumber(value)) {
            return SerializeResult.success(Component.text(String.valueOf(context.asNumber(value))));
        }
        if(context.isList(value)) {
            return deserializeList(context, context.asList(value), version);
        }
        if(context.isMap(value)) {
            return deserializeMap(context, value, version);
        }

        return SerializeResult.failure("Unable to turn " + value + " into a component!");
    }


    private <O> SerializeResult<Component> deserializeList(SerializeContext<O> context, Collection<O> value, GameVersion version) {
        List<Component> values = new ArrayList<>();

        if(value.isEmpty()) {
            return SerializeResult.success(Component.text(""));
        }

        for(O o : value) {

            SerializeResult<Component> res = deserialize(context, o, version);
            if(!res.isComplete()) return res;

            values.add(res.getOrThrow());
        }

        return SerializeResult.success(values.get(0).addChildren(values.subList(1, values.size())));
    }

    private <O> SerializeResult<Component> deserializeMap(SerializeContext<O> context, O value, GameVersion version) {

        MutableComponent out = null;
        for(String s : context.getOrderedKeys(value)) {

            Content.Type type = Content.Type.byId(s);
            if(type != null) {

                ContentSerializer.Context ctx = new ContentSerializer.Context(version, this);
                SerializeResult<? extends Content> con = getSerializer(type).deserialize(context, value, ctx);
                if(!con.isComplete()) {
                    return SerializeResult.failure("Unable to deserialize component contents! " + con.getError());
                }

                out = new MutableComponent(con.getOrThrow());
                break;
            }
        }

        if(out == null) {
            return SerializeResult.failure("Unable to deserialize component contents! Could not find valid content type!");
        }

        String color = Serializer.STRING.deserialize(context, context.get("color", value)).get().orElse(null);
        if(color != null) {
            if(color.equals("reset")) {
                out.reset = true;
            } else {
                out.color = TextColor.parse(color);
            }
        }

        out.bold = Serializer.BOOLEAN.deserialize(context, context.get("bold", value)).get().orElse(null);
        out.italic = Serializer.BOOLEAN.deserialize(context, context.get("italic", value)).get().orElse(null);
        out.underlined = Serializer.BOOLEAN.deserialize(context, context.get("underlined", value)).get().orElse(null);
        out.strikethrough = Serializer.BOOLEAN.deserialize(context, context.get("strikethrough", value)).get().orElse(null);
        out.obfuscated = Serializer.BOOLEAN.deserialize(context, context.get("obfuscated", value)).get().orElse(null);
        out.font = Identifier.serializer("minecraft").deserialize(context, context.get("font", value)).get().orElse(null);
        out.insertion = Serializer.STRING.deserialize(context, context.get("insertion", value)).get().orElse(null);
        out.hoverEvent = HoverEvent.SERIALIZER.deserialize(context, context.get("hoverEvent", value), version).get().orElse(null);
        out.clickEvent = ClickEvent.SERIALIZER.deserialize(context, context.get("clickEvent", value)).get().orElse(null);
        out.shadowColor = Color.SERIALIZER.deserialize(context, context.get("shadow_color", value)).get().map(Color::asRGBA).orElse(null);

        O extra = context.get("extra", value);
        if(context.isList(extra)) {

            Collection<O> values = context.asList(extra);
            for(O o : values) {
                SerializeResult<Component> child = deserialize(context, o, version);
                if(!child.isComplete()) {
                    return SerializeResult.failure("Unable to deserialize component extra! " + child.getError());
                }
                out.addChild(child.getOrThrow());
            }
        }

        return SerializeResult.success(out.toComponent());
    }

    public static final ContentSerializer<Content.Text> TEXT = new ContentSerializer<>(Content.Text.class,
            ContextSerializer.fromStatic(ObjectSerializer.create(
                    Serializer.STRING.entry("text", (text) -> text.text),
                    Content.Text::new
            ))
    );

    public static final ContentSerializer<Content.Translate> TRANSLATE = new ContentSerializer<>(Content.Translate.class,
            ContextObjectSerializer.create(
                    Serializer.STRING.entry("translate", (translate, c) -> translate.key),
                    Serializer.STRING.<Content.Translate, ContentSerializer.Context>entry("fallback", (translate, context) -> translate.fallback).optional(),
                    ContentSerializer.COMPONENT.listOf().<Content.Translate>entry("with", (translate, context) -> translate.with).optional(),
                    (c, key, fallback, with) -> new Content.Translate(key, fallback, with)
            )
    );

    public static final ContentSerializer<Content.Keybind> KEYBIND = new ContentSerializer<>(Content.Keybind.class,
            ContextSerializer.fromStatic(ObjectSerializer.create(
                    Serializer.STRING.entry("keybind", con -> con.key),
                    Content.Keybind::new
            ))
    );

    public static final ContentSerializer<Content.Score> SCORE = new ContentSerializer<>(Content.Score.class,
            ContextSerializer.fromStatic(ObjectSerializer.create(
                    ConfigSection.SERIALIZER.entry("score", con -> new ConfigSection()
                            .with("name", con.name)
                            .with("objective", con.objective)
                            .with("value", con.value)),
                    (cfg) -> new Content.Score(
                            cfg.getString("name"),
                            cfg.getOrDefault("objective", (String) null),
                            cfg.getOrDefault("value", (String) null)
                    )
            ))
    );

    public static final ContentSerializer<Content.Selector> SELECTOR = new ContentSerializer<>(Content.Selector.class,
            ContextSerializer.fromStatic(ObjectSerializer.create(
                    Serializer.STRING.entry("selector", con -> con.value),
                    Content.Selector::new
            ))
    );

    public static final ContentSerializer<Content.NBT> NBT = new ContentSerializer<>(Content.NBT.class,
            ContextObjectSerializer.create(
                    Serializer.STRING.entry("nbt", (nbt, ctx) -> nbt.path),
                    Serializer.BOOLEAN.<Content.NBT, ContentSerializer.Context>entry("interpret", (nbt, ctx) -> nbt.interpret).optional(),
                    ContentSerializer.COMPONENT.<Content.NBT>entry("separator", (nbt, ctx) -> nbt.separator).optional(),
                    Serializer.STRING.<Content.NBT, ContentSerializer.Context>entry("block", (nbt, ctx) -> nbt.type == Content.NBT.DataSourceType.BLOCK ? nbt.data : null).optional(),
                    Serializer.STRING.<Content.NBT, ContentSerializer.Context>entry("entity", (nbt, ctx) -> nbt.type == Content.NBT.DataSourceType.ENTITY ? nbt.data : null).optional(),
                    Serializer.STRING.<Content.NBT, ContentSerializer.Context>entry("storage",(nbt, ctx) -> nbt.type == Content.NBT.DataSourceType.STORAGE ? nbt.data : null).optional(),
                    (ctx, path, interpret, sep, block, entity, storage) -> {

                        String data;
                        Content.NBT.DataSourceType type;
                        if(block != null) {
                            data = block;
                            type = Content.NBT.DataSourceType.BLOCK;
                        }
                        else if(entity != null) {
                            data = entity;
                            type = Content.NBT.DataSourceType.ENTITY;
                        }
                        else if(storage != null) {
                            data = storage;
                            type = Content.NBT.DataSourceType.STORAGE;
                        } else {
                            throw new IllegalArgumentException("Not enough data to deserialize NBT component!");
                        }

                        return new Content.NBT(path, interpret, sep, type, data);
                    }
            ));

    private static ContentSerializer<?> getSerializer(Content.Type type) {
        switch (type) {
            case TEXT:
                return TEXT;
            case TRANSLATE:
                return TRANSLATE;
            case KEYBIND:
                return KEYBIND;
            case SCORE:
                return SCORE;
            case SELECTOR:
                return SELECTOR;
            case NBT:
                return NBT;
            default:
                return null;
        }
    }
}
