package org.wallentines.mcore.text;

import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.ItemStack;
import org.wallentines.mcore.util.ItemUtil;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.SNBTCodec;
import org.wallentines.mdcfg.serializer.*;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

import java.util.*;
import java.util.function.Function;

public class HoverEvent<T> {

    private final Type<T> type;
    private final T value;

    public HoverEvent(Type<T> type, T value) {
        this.type = type;
        this.value = value;
    }

    public Type<T> getType() {
        return type;
    }

    public String getTypeId() {
        return TYPES.getId(type);
    }

    public T getValue() {
        return value;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoverEvent<?> that = (HoverEvent<?>) o;
        return Objects.equals(type, that.type) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    public static HoverEvent<Component> create(Component component) {
        return new HoverEvent<>(Type.SHOW_TEXT, component);
    }

    public static HoverEvent<ItemStack> forItem(ItemStack itemStack) {
        return new HoverEvent<>(Type.SHOW_ITEM, itemStack);
    }

    public static HoverEvent<EntityInfo> forEntity(EntityInfo entity) {
        return new HoverEvent<>(Type.SHOW_ENTITY, entity);
    }

    private static SNBTCodec snbt(GameVersion version) {
        SNBTCodec out = new SNBTCodec();
        if(version.getProtocolVersion() < 477) {
            out.useDoubleQuotes();
        }
        return out;
    }

// = new ContextSerializer<>() {
//        @Override
//        public <O> SerializeResult<O> serialize(SerializeContext<O> context, HoverEvent<?> event, GameVersion version) {
//            return serializeGeneric(context, event, version);
//        }
//
//        private <GT,O> SerializeResult<O> serializeGeneric(SerializeContext<O> context, HoverEvent<GT> event, GameVersion version) {
//            String id = TYPES.getId(event.type);
//            if(id == null) {
//                return SerializeResult.failure("Attempt to serialized unregistered HoverEvent type " + event.type + "!");
//            }
//
//            O out = context.toMap(new HashMap<>());
//            context.set("action", context.toString(id), out);
//
//            return event.type.serializer.serialize(context, event.value, version).map(o -> {
//
//                if(version.hasFeature(GameVersion.Feature.HOVER_CONTENTS)) {
//                    context.set("contents", o, out);
//                } else {
//                    if(event.type == Type.SHOW_TEXT || event.type == Type.SHOW_ACHIEVEMENT) {
//                        context.set("value", o, out);
//                    } else {
//                        context.set("value", context.toString(snbt(version).encodeToString(context, o)), out);
//                    }
//                }
//                return SerializeResult.success(out);
//            });
//        }
//
//        @Override
//        public <O> SerializeResult<HoverEvent<?>> deserialize(SerializeContext<O> context, O value, GameVersion version) {
//
//            if(!context.isMap(value)) {
//                return SerializeResult.failure("Don't know how to deserialize " + value + " as a HoverEvent!");
//            }
//
//            O oid = context.get("action", value);
//            if(!context.isString(oid)) {
//                return SerializeResult.failure("Can't deserialize HoverEvent! Missing key action!");
//            }
//
//            String id = context.asString(oid);
//            Type<?> type = TYPES.get(id);
//            if(type == null) {
//                return SerializeResult.failure("Can't deserialize HoverEvent! No action of type " + oid + "!");
//            }
//
//            return type.deserialize(context, value, version).flatMap(th -> th);
//        }
//    };


    public static final Registry<String, HoverEvent.Type<?>> TYPES = Registry.createStringRegistry();

    public static final Serializer<HoverEvent<?>> SERIALIZER = TYPES.byIdSerializer().fieldOf("action").dispatch(
            HoverEvent::fromType,
            HoverEvent::fromEvent
    );

    private static <T> Type<T> fromEvent(HoverEvent<T> event) {
        return event.getType();
    }

    private static <T> Serializer<HoverEvent<?>> fromType(Type<T> type) {
        return type.getSerializer().fieldOf("contents").map(
                he -> SerializeResult.success(he.getValue()).cast(type.valueType),
                val -> SerializeResult.success(new HoverEvent<>(type, val))
        );
    }


    public static class Type<T> {
        private final Serializer<T> serializer;
        private final Class<T> valueType;

        public Type(Serializer<T> serializer, Class<T> valueType) {
            this.serializer = serializer;
            this.valueType = valueType;
        }

        public Serializer<T> getSerializer() {
            return serializer;
        }

        public HoverEvent<T> create(T value) {
            return new HoverEvent<>(this, value);
        }

        public <O> SerializeResult<HoverEvent<T>> deserialize(SerializeContext<O> context, O value, GameVersion version) {

            String contentName = version.hasFeature(GameVersion.Feature.HOVER_CONTENTS) ? "contents" : "value";
            O content = context.get(contentName, value);

            return getSerializer().deserialize(context, content).flatMap(this::create);
        }

        /**
         * Shows text in a tooltip when a component is hovered over
         */
        public static final HoverEvent.Type<Component> SHOW_TEXT = register("show_text", ModernSerializer.INSTANCE, Component.class);

        /**
         * Shows an item tooltip when a component is hovered over
         */
        public static final HoverEvent.Type<ItemStack> SHOW_ITEM = register("show_item", new Serializer<ItemStack>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> ctx, ItemStack value) {

                GameVersion version = GameVersion.getVersion(ctx);

                ConfigSection out = new ConfigSection();
                out.set("id", value.getType().toString());

                String countKey = version.hasFeature(GameVersion.Feature.HOVER_CONTENTS) ? "count" : "Count";
                out.set(countKey, value.getCount());

                if(!version.hasFeature(GameVersion.Feature.NAMESPACED_IDS)) {
                    out.set("Damage", value.getLegacyDataValue());
                }

                if(version.hasFeature(GameVersion.Feature.ITEM_COMPONENTS)) {
                    out.set("components", ItemStack.ComponentPatchSet.fromItem(value), ItemStack.ComponentPatchSet.SERIALIZER);
                } else {
                    if(version.hasFeature(GameVersion.Feature.HOVER_CONTENTS)) {
                        out.set("tag", snbt(version).encodeToString(ConfigContext.INSTANCE, value.getCustomData()));
                    } else {
                        out.set("tag", value.getCustomData());
                    }
                }

                return SerializeResult.success(ConfigContext.INSTANCE.convert(ctx, out));
            }

            @Override
            public <O> SerializeResult<ItemStack> deserialize(SerializeContext<O> ctx, O value) {

                GameVersion version = GameVersion.getVersion(ctx);
                ConfigSection sec = ctx.convert(ConfigContext.INSTANCE, value).asSection();
                String sid = sec.getOrDefault("id", (String) null);
                if(sid == null) {
                    return SerializeResult.failure("Item id is required!");
                }

                Identifier id;
                try {
                    id = Identifier.parseOrDefault(sid, "minecraft");
                } catch (IllegalArgumentException ex) {
                    return SerializeResult.failure("Unable to parse item ID! " + ex.getMessage());
                }
                ItemStack.Builder builder = ItemStack.Builder.of(version, id);

                String countKey = version.hasFeature(GameVersion.Feature.HOVER_CONTENTS) ? "count" : "Count";

                if(sec.has(countKey)) {
                    builder.withCount(sec.getOrDefault(countKey, 1).intValue());
                }

                if(!version.hasFeature(GameVersion.Feature.NAMESPACED_IDS) && sec.has("Damage")) {
                    builder.withDataValue(sec.getOrDefault("Damage", 0).byteValue());
                }

                if(version.hasFeature(GameVersion.Feature.ITEM_COMPONENTS)) {
                    if(sec.has("components")) {
                        Optional<ItemStack.ComponentPatchSet> opt = sec.getOptional("components", ItemStack.ComponentPatchSet.SERIALIZER);
                        if(opt.isPresent()) {
                            builder.withComponents(opt.get());
                        } else {
                            return SerializeResult.failure("Unable to parse item components!");
                        }
                    }
                } else {
                    if(sec.has("tag")) {
                        ConfigSection tag;
                        if (version.hasFeature(GameVersion.Feature.HOVER_CONTENTS)) {
                            ConfigObject obj = snbt(version).decode(ConfigContext.INSTANCE, sec.getString("tag"));
                            if (!obj.isString()) {
                                return SerializeResult.failure("Expected item tag to be an SNBT string!");
                            }
                            tag = obj.asSection();
                        } else {
                            tag = sec.getSection("tag");
                        }
                        builder.withCustomData(tag);
                    }
                }

                return SerializeResult.success(builder.build());
            }
        }, ItemStack.class);
//        public static final HoverEvent.Type<ItemStack> SHOW_ITEM = register("show_item", ContextObjectSerializer.create(
//                Identifier.serializer("minecraft").entry("id", (is, ver) -> is.getType()),
//                Serializer.INT.<ItemStack,GameVersion>entry(ver ->
//                        ver.hasFeature(GameVersion.Feature.HOVER_CONTENTS) ? "count" : "Count",
//                        (is,ver) -> is.getCount()).orElse(ctx -> 1),
//                Serializer.BYTE.<ItemStack,GameVersion>entry("Damage", (is,ver) -> ver.hasFeature(GameVersion.Feature.NAMESPACED_IDS) ? null : is.getLegacyDataValue()).optional(),
//                ConfigSection.SERIALIZER.
//                        <ItemStack,GameVersion>entry("tag", (is, ver) -> ver.hasFeature(GameVersion.Feature.ITEM_COMPONENTS) ? null : is.getCustomData()).optional(),
//                ItemStack.ComponentPatchSet.SERIALIZER.<ItemStack, GameVersion>entry("components", (is, ver) -> ver .hasFeature(GameVersion.Feature.ITEM_COMPONENTS) ? ItemStack.ComponentPatchSet.fromItem(is) : null).optional(),
//                (ver, id, count, data, tag, components) ->
//                    ItemStack.Builder.of(ver, id)
//                        .withCount(count)
//                        .withDataValue(data)
//                        .withCustomData(tag)
//                        .withComponents(components)
//                        .build()
//        ));


        /**
         * Shows entity data when a component is hovered over
         */
        public static final HoverEvent.Type<EntityInfo> SHOW_ENTITY = register("show_entity",
                ObjectSerializer.create(
                        ItemUtil.UUID_SERIALIZER.<EntityInfo>entry("id", (ei) -> ei.uuid),
                        Identifier.serializer("minecraft").<EntityInfo>entry("type", (ei) -> ei.type).orElse(new Identifier("minecraft", "pig")),
                        ModernSerializer.INSTANCE.<EntityInfo>entry("name", (ei) -> ei.name),
                        (uuid, identifier, component) -> new EntityInfo(component, identifier, uuid)
                ),
                EntityInfo.class
        );

        /**
         * Shows an achievement name and description when a component is hovered over.
         * Not used since pre-1.12
         */
        public static final HoverEvent.Type<String> SHOW_ACHIEVEMENT = register("show_achievement", InlineSerializer.RAW, String.class);

    }

    public static class EntityInfo {

        public final Component name;
        public final Identifier type;
        public final UUID uuid;

        public EntityInfo(Component name, Identifier type, UUID uuid) {
            this.name = name;
            this.type = type;
            this.uuid = uuid;
        }
    }


    private static <T> HoverEvent.Type<T> register(String id, Serializer<T> serializer, Class<T> valueType) {
        Type<T> type = new Type<>(serializer, valueType);
        HoverEvent.TYPES.register(id, type);
        return type;
    }

}
