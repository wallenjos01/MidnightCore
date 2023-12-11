package org.wallentines.mcore.text;

import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.ItemStack;
import org.wallentines.mcore.VersionSerializer;
import org.wallentines.mcore.util.ItemUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.*;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.StringRegistry;

import java.util.HashMap;
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

    public static HoverEvent<Component> create(Component component) {
        return new HoverEvent<>(Type.SHOW_TEXT, component);
    }

    public static HoverEvent<ItemStack> forItem(ItemStack itemStack) {
        return new HoverEvent<>(Type.SHOW_ITEM, itemStack);
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
        private final VersionSerializer<T> serializer;

        public Type(VersionSerializer<T> serializer) {
            this.serializer = serializer;
        }

        public VersionSerializer<T> getSerializer() {
            return serializer;
        }

        public HoverEvent<T> create(T value) {
            return new HoverEvent<>(this, value);
        }

        public <O> SerializeResult<HoverEvent<T>> deserialize(SerializeContext<O> context, O value, GameVersion version) {

            return getSerializer().deserialize(context, value, version).flatMap(this::create);
        }

        /**
         * Shows text in a tooltip when a component is hovered over
         */
        public static final HoverEvent.Type<Component> SHOW_TEXT = register("show_text", ModernSerializer.INSTANCE);

        /**
         * Shows an item tooltip when a component is hovered over
         */
        public static final HoverEvent.Type<ItemStack> SHOW_ITEM = register("show_item", new VersionSerializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, ItemStack value, GameVersion version) {

                O out = context.toMap(new HashMap<>());
                boolean modern = version.hasFeature(GameVersion.Feature.NAMESPACED_IDS);
                boolean count = version.hasFeature(GameVersion.Feature.HOVER_CONTENTS);

                context.set("id", context.toString(value.getType().toString()), out);
                context.set(count ? "count" : "Count", context.toNumber(value.getCount()), out);
                ConfigSection tag = value.getTag();
                if(tag != null) {
                    context.set("tag", ConfigContext.INSTANCE.convert(context, tag), out);
                }
                if(!modern) {
                    context.set("Damage", context.toNumber(value.getLegacyDataValue()), out);
                    return SerializeResult.success(context.toString(ItemUtil.toNBTString(context, out)));
                }

                return SerializeResult.success(out);
            }

            @Override
            public <O> SerializeResult<ItemStack> deserialize(SerializeContext<O> context, O value, GameVersion version) {
                return SerializeResult.failure("show_item contents cannot be deserialized this way!");
            }
        });

        /**
         * Shows entity data when a component is hovered over
         */
        public static final HoverEvent.Type<EntityInfo> SHOW_ENTITY = register("show_entity", new VersionSerializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, EntityInfo value, GameVersion version) {

                O out = context.toMap(new HashMap<>());

                SerializeResult<O> uuid = Serializer.UUID.serialize(context, value.uuid);
                if(!uuid.isComplete()) {
                    return SerializeResult.failure("Unable to serialize EntityInfo name! " + uuid.getError());
                }
                context.set("uuid", uuid.getOrThrow(), out);

                if(value.type != null) {
                    SerializeResult<O> type = Identifier.serializer("minecraft").serialize(context, value.type);
                    if (!type.isComplete()) {
                        return SerializeResult.failure("Unable to serialize EntityInfo type! " + type.getError());
                    }
                    context.set("type", type.getOrThrow(), out);
                }

                if(value.name != null) {

                    SerializeResult<O> name = ModernSerializer.INSTANCE.serialize(context, value.name, version);
                    if(!name.isComplete()) {
                        return SerializeResult.failure("Unable to serialize EntityInfo name! " + name.getError());
                    }
                    context.set("name", context.toString(ItemUtil.toNBTString(context, name.getOrThrow())), out);
                }

                return SerializeResult.success(out);
            }

            @Override
            public <O> SerializeResult<EntityInfo> deserialize(SerializeContext<O> context, O value, GameVersion version) {
                return SerializeResult.failure("show_entity contents cannot be deserialized this way!");
            }
        });

        /**
         * Shows an achievement name and description when a component is hovered over.
         * Not used since pre-1.12
         */
        public static final HoverEvent.Type<String> SHOW_ACHIEVEMENT = register("show_achievement", VersionSerializer.fromStatic(InlineSerializer.RAW));

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

    private static <T> HoverEvent.Type<T> register(String id, VersionSerializer<T> serializer) {
        Type<T> type = new Type<>(serializer);
        HoverEvent.TYPES.register(id, type);
        return type;
    }



}
