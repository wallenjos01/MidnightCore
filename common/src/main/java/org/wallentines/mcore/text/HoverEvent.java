package org.wallentines.mcore.text;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.ItemStack;
import org.wallentines.mcore.VersionSerializer;
import org.wallentines.mcore.util.ItemUtil;
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

    public static HoverEvent<ItemInfo> forItem(ItemStack itemStack) {
        return new HoverEvent<>(Type.SHOW_ITEM, new ItemInfo(itemStack));
    }

    public static HoverEvent<EntityInfo> forEntity(EntityInfo entity) {
        return new HoverEvent<>(Type.SHOW_ENTITY, entity);
    }


    public static final VersionSerializer<HoverEvent<?>> SERIALIZER = new VersionSerializer<>() {
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
            String key = version.hasFeature(GameVersion.Feature.HOVER_CONTENTS) ? "contents" : "value";

            return event.type.serializer.serialize(context, event.value, version).map(o -> {
                context.set(key, o, out);
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
        public static final HoverEvent.Type<ItemInfo> SHOW_ITEM = register("show_item", ObjectSerializer.createContextAware(
                Identifier.serializer("minecraft").<ItemInfo, GameVersion>entry("id", (is, ver) -> is.id),
                Serializer.INT.<ItemInfo, GameVersion>entry("count", (is,ver) -> ver.hasFeature(GameVersion.Feature.HOVER_CONTENTS) ? is.count : null).optional(),
                Serializer.INT.<ItemInfo, GameVersion>entry("Count", (is,ver) -> ver.hasFeature(GameVersion.Feature.HOVER_CONTENTS) ? null : is.count).optional(),
                Serializer.BYTE.<ItemInfo, GameVersion>entry("Damage", (is,ver) -> ver.hasFeature(GameVersion.Feature.NAMESPACED_IDS) ? null : is.data).optional(),
                Serializer.STRING.<ItemInfo, GameVersion>entry("tag", (is,ver) -> is.tag).optional(),
                (ver, id, mCount, lCount, lData, tag) -> {

                    Integer count = ver.hasFeature(GameVersion.Feature.HOVER_CONTENTS) ? mCount : lCount;
                    Byte data = lData == null || !ver.hasFeature(GameVersion.Feature.NAMESPACED_IDS) ? null : lData;

                    return new ItemInfo(id, count, tag, data);
                })
        );

        /**
         * Shows entity data when a component is hovered over
         */
        public static final HoverEvent.Type<EntityInfo> SHOW_ENTITY = register("show_entity",
                ObjectSerializer.createContextAware(
                        Serializer.UUID.entry("uuid", (ei, ver) -> ei.uuid),
                        Identifier.serializer("minecraft").<EntityInfo, GameVersion>entry("type", (ei, ver) -> ei.type).orElse(ver -> new Identifier("minecraft", "pig")),
                        ModernSerializer.INSTANCE.entry("name", (ei, ver) -> ei.name),
                        (version, uuid, identifier, component) -> new EntityInfo(component, identifier, uuid)
                )
        );

        /**
         * Shows an achievement name and description when a component is hovered over.
         * Not used since pre-1.12
         */
        public static final HoverEvent.Type<String> SHOW_ACHIEVEMENT = register("show_achievement", VersionSerializer.fromStatic(InlineSerializer.RAW));

    }

    public static class ItemInfo {
        public final Identifier id;
        public final Integer count;
        public final Byte data;

        private String tag;
        private final ItemStack item;

        public ItemInfo(Identifier id, Integer count, String tag, Byte data) {
            this.id = id;
            this.count = count;
            this.tag = tag;
            this.data = data;
            this.item = null;
        }

        public ItemInfo(ItemStack itemStack) {
            this.id = itemStack.getType();
            this.count = itemStack.getCount();
            this.data = itemStack.getLegacyDataValue();
            this.item = itemStack;
        }

        @Nullable
        public ItemStack getItem() {
            return item;
        }

        public String getTag() {
            if(tag == null && item != null && item.getTag() != null) {
                tag = ItemUtil.toNBTString(ConfigContext.INSTANCE, item.getTag());
            }
            return tag;
        }

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
