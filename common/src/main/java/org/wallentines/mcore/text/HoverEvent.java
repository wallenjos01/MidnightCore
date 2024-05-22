package org.wallentines.mcore.text;

import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.ItemStack;
import org.wallentines.mcore.util.ItemUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.SNBTCodec;
import org.wallentines.mdcfg.serializer.*;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.StringRegistry;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

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

    public static final ContextSerializer<HoverEvent<?>, GameVersion> SERIALIZER = new ContextSerializer<>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, HoverEvent<?> event, GameVersion version) {
            return serializeGeneric(context, event, version);
        }

        private <GT,O> SerializeResult<O> serializeGeneric(SerializeContext<O> context, HoverEvent<GT> event, GameVersion version) {
            String id = TYPES.getId(event.type);
            if(id == null) {
                return SerializeResult.failure("Attempt to serialized unregistered HoverEvent type " + event.type + "!");
            }

            O out = context.toMap(new HashMap<>());
            context.set("action", context.toString(id), out);

            return event.type.serializer.serialize(context, event.value, version).map(o -> {
                if(version.hasFeature(GameVersion.Feature.HOVER_CONTENTS)) {
                    context.set("contents", o, out);
                } else {
                    if(event.type == Type.SHOW_TEXT || event.type == Type.SHOW_ACHIEVEMENT) {
                        context.set("value", o, out);
                    } else {
                        context.set("value", context.toString(snbt(version).encodeToString(context, o)), out);
                    }
                }
                return SerializeResult.success(out);
            });
        }

        @Override
        public <O> SerializeResult<HoverEvent<?>> deserialize(SerializeContext<O> context, O value, GameVersion version) {

            if(!context.isMap(value)) {
                return SerializeResult.failure("Don't know how to deserialize " + value + " as a HoverEvent!");
            }

            O oid = context.get("action", value);
            if(!context.isString(oid)) {
                return SerializeResult.failure("Can't deserialize HoverEvent! Missing key action!");
            }

            String id = context.asString(oid);
            Type<?> type = TYPES.get(id);
            if(type == null) {
                return SerializeResult.failure("Can't deserialize HoverEvent! No action of type " + oid + "!");
            }

            return type.deserialize(context, value, version).flatMap(th -> th);
        }
    };


    public static class Type<T> {
        private final ContextSerializer<T, GameVersion> serializer;

        public Type(ContextSerializer<T, GameVersion> serializer) {
            this.serializer = serializer;
        }

        public ContextSerializer<T, GameVersion> getSerializer() {
            return serializer;
        }

        public HoverEvent<T> create(T value) {
            return new HoverEvent<>(this, value);
        }

        public <O> SerializeResult<HoverEvent<T>> deserialize(SerializeContext<O> context, O value, GameVersion version) {

            String contentName = version.hasFeature(GameVersion.Feature.HOVER_CONTENTS) ? "contents" : "value";
            O content = context.get(contentName, value);

            return getSerializer().deserialize(context, content, version).flatMap(this::create);
        }

        /**
         * Shows text in a tooltip when a component is hovered over
         */
        public static final HoverEvent.Type<Component> SHOW_TEXT = register("show_text", ModernSerializer.INSTANCE);

        /**
         * Shows an item tooltip when a component is hovered over
         */
        public static final HoverEvent.Type<ItemStack> SHOW_ITEM = register("show_item", ContextObjectSerializer.create(
                Identifier.serializer("minecraft").entry("id", (is, ver) -> is.getType()),
                Serializer.INT.<ItemStack,GameVersion>entry(ver ->
                        ver.hasFeature(GameVersion.Feature.HOVER_CONTENTS) ? "count" : "Count",
                        (is,ver) -> is.getCount()).orElse(ctx -> 1),
                Serializer.BYTE.<ItemStack,GameVersion>entry("Damage", (is,ver) -> ver.hasFeature(GameVersion.Feature.NAMESPACED_IDS) ? null : is.getLegacyDataValue()).optional(),
                ConfigSection.SERIALIZER.<ItemStack,GameVersion>entry("tag", (is, ver) -> ver.hasFeature(GameVersion.Feature.ITEM_COMPONENTS) ? null : is.getCustomData()).optional(),
                ItemStack.ComponentPatchSet.SERIALIZER.<ItemStack, GameVersion>entry("components", (is, ver) -> ver .hasFeature(GameVersion.Feature.ITEM_COMPONENTS) ? ItemStack.ComponentPatchSet.fromItem(is) : null).optional(),
                (ver, id, count, data, tag, components) ->
                    ItemStack.Builder.of(ver, id)
                        .withCount(count)
                        .withDataValue(data)
                        .withCustomData(tag)
                        .withComponents(components)
                        .build()
        ));


        /**
         * Shows entity data when a component is hovered over
         */
        public static final HoverEvent.Type<EntityInfo> SHOW_ENTITY = register("show_entity",
                ContextObjectSerializer.create(
                        ItemUtil.UUID_SERIALIZER.entry("id", (ei, ver) -> ei.uuid),
                        Identifier.serializer("minecraft").<EntityInfo, GameVersion>entry("type", (ei, ver) -> ei.type).orElse(ver -> new Identifier("minecraft", "pig")),
                        ModernSerializer.INSTANCE.entry("name", (ei, ver) -> ei.name),
                        (version, uuid, identifier, component) -> new EntityInfo(component, identifier, uuid)
                )
        );

        /**
         * Shows an achievement name and description when a component is hovered over.
         * Not used since pre-1.12
         */
        public static final HoverEvent.Type<String> SHOW_ACHIEVEMENT = register("show_achievement", ContextSerializer.fromStatic(InlineSerializer.RAW));

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

    public static final StringRegistry<HoverEvent.Type<?>> TYPES = new StringRegistry<>();

    private static <T> HoverEvent.Type<T> register(String id, ContextSerializer<T, GameVersion> serializer) {
        Type<T> type = new Type<>(serializer);
        HoverEvent.TYPES.register(id, type);
        return type;
    }



}
