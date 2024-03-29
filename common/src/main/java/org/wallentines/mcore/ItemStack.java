package org.wallentines.mcore;

import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.LegacySerializer;
import org.wallentines.mcore.text.ModernSerializer;
import org.wallentines.mcore.text.TextColor;
import org.wallentines.mcore.util.ItemUtil;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.*;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.types.Singleton;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A data type representing a Minecraft item-stack
 */
public interface ItemStack {

    /**
     * Returns the ID of the type of item this is
     * @return The item type ID
     */
    Identifier getType();

    /**
     * Returns the amount of items in the stack
     * @return The item count
     */
    int getCount();

    /**
     * Returns the NBT tag of the item stack
     * @return The NBT tag of the item stack
     */
    ConfigSection getTag();

    /**
     * Returns the legacy data value of an item (may not work properly on versions >= 1.13)
     * @return The legacy data value of the item stack
     */
    byte getLegacyDataValue();

    /**
     * Changes the amount of items in the stack
     * @param count The new number of items
     */
    void setCount(int count);

    /**
     * Changes the NBT tag of the item
     * @param tag the new NBT tag
     */
    void setTag(ConfigSection tag);

    /**
     * Increases the amount of items in the stack by the given amount.
     * @param amount The amount to grow the stack by
     */
    void grow(int amount);

    /**
     * Decreases the amount of items in the stack by the given amount.
     * @param amount The amount to shrink the stack by
     */
    void shrink(int amount);

    /**
     * Gets the translation key for this item type, for use in translate Components.
     * @return The translation key for this item type
     */
    String getTranslationKey();


    /**
     * Gets the version of the game this item is native to
     * @return The item's game version.
     */
    GameVersion getVersion();

    /**
     * Makes an exact copy of this item stack
     * @return An exact copy
     */
    default ItemStack copy() {
        return Builder.of(getVersion(), getType()).withCount(getCount()).withTag(getTag()).withDataValue(getLegacyDataValue()).build();
    }

    /**
     * Changes the display name of the item
     * @param component The new display name
     */
    default void setName(Component component) {
        component = ItemUtil.applyItemNameBaseStyle(component);
        String strName = getVersion().hasFeature(GameVersion.Feature.ITEM_NAME_COMPONENTS) ?
                component.toJSONString() :
                component.toLegacyText();

        ConfigSection tag = getTag();
        tag.getOrCreateSection("display").set("Name", strName);
        setTag(tag);
    }


    /**
     * Gets the display name of an item as it appears when hovering over it, for the maximum supported version
     * @return The item's custom name if available, or a translate component
     */
    default Component getName() {
        return getName(GameVersion.MAX);
    }

    /**
     * Gets the display name of an item as it appears when hovering over it
     * @param version The game version to interpret the item as coming from
     * @return The item's custom name if available, or a translate component
     */
    default Component getName(GameVersion version) {

        ConfigSection tag = getTag();
        Component def = Component.translate(getTranslationKey()).withColor(getRarityColor());
        if(tag == null || !tag.has("display")) {
            return def;
        }

        ConfigSection display = tag.getSection("display");
        if(!display.hasString("Name")) {
            return def;
        }

        SerializeResult<Component> comp;
        if(version.hasFeature(GameVersion.Feature.ITEM_NAME_COMPONENTS)) {
            comp = ModernSerializer.INSTANCE.deserialize(ConfigContext.INSTANCE, JSONCodec.minified().decode(ConfigContext.INSTANCE, display.getString("Name")), version);
        } else {
            comp = LegacySerializer.INSTANCE.deserialize(ConfigContext.INSTANCE, new ConfigPrimitive(display.getString("Name")));
        }

        if(comp.isComplete()) {
            Component out = comp.getOrThrow();
            if(out.italic == null) out = out.withItalic(true);
            if(out.color == null) out = out.withColor(getRarityColor());
            return out;
        }

        return def;
    }

    /**
     * Gets the color associated with the item's rarity
     * @return The item's rarity color.
     */
    Color getRarityColor();

    /**
     * Constructs an empty (Air) item stack
     * @return An empty item stack
     */
    static ItemStack empty() {
        return Builder.of(GameVersion.MAX, new Identifier("minecraft", "air")).build();
    }


    /**
     * Will contain an item factory by the time the game loads
     */
    Singleton<Factory> FACTORY = new Singleton<>();

    ContextSerializer<ItemStack, GameVersion> VERSION_SERIALIZER = ObjectSerializer.createContextAware(
            Identifier.serializer("minecraft").entry("type", (is, ver) -> is.getType()),
            NumberSerializer.forInt(1,64).<ItemStack, GameVersion>entry("count", (is,ver) -> is.getCount()).orElse(v -> 1),
            ConfigSection.SERIALIZER.<ItemStack, GameVersion>entry("tag", (is,ver) -> is.getTag()).optional(),
            NumberSerializer.forByte((byte) 0,(byte) 15).<ItemStack, GameVersion>entry("data", (is,ver) -> is.getLegacyDataValue()).optional(),
            (ver, type, count, tag, data) -> FACTORY.get().build(type, count, tag, data, ver)
    );

    Serializer<ItemStack> SERIALIZER = VERSION_SERIALIZER.forContext(GameVersion.CURRENT_VERSION::get);

    /**
     * An interface to construct an item stack
     */
    interface Factory {
        ItemStack build(Identifier type, int count, ConfigSection tag, byte legacyData, GameVersion version);
    }


    /**
     * A builder type to easily create item stacks.
     */
    class Builder {

        private final GameVersion version;
        private final Identifier id;
        private int count = 1;
        private ConfigSection tag = null;
        private byte dataValue = 0;

        private Builder(GameVersion version, Identifier id) {
            this.version = version;
            this.id = id;
        }

        private ConfigSection getOrCreateTag() {

            if(this.tag == null) {
                this.tag = new ConfigSection();
            }
            return this.tag;
        }

        /**
         * Changes the count of the item stack to be built
         * @param count The new count of the item stack
         * @return A reference to self
         */
        public Builder withCount(int count) {
            this.count = count;
            return this;
        }

        /**
         * Changes the NBT tag of the item stack to be built
         * @param tag The new NBT tag of the item stack
         * @return A reference to self
         */
        public Builder setTag(ConfigSection tag) {
            this.tag = tag;
            return this;
        }

        /**
         * Adds the given data to the NBT tag of the item stack to be built
         * @param tag The new NBT tag of the item stack
         * @return A reference to self
         */
        public Builder withTag(ConfigSection tag) {

            if(tag == null) {
                this.tag = null;
                return this;
            }

            getOrCreateTag().fillOverwrite(tag);
            return this;
        }

        /**
         * Changes the legacy data value of the item stack to be built
         * @param dataValue The new NBT tag of the item stack
         * @return A reference to self
         */
        public Builder withDataValue(byte dataValue) {
            if(dataValue != 0 && version.hasFeature(GameVersion.Feature.NAMESPACED_IDS)) {
                MidnightCoreAPI.LOGGER.warn("ItemStack data value specified on a version which does not support it!");
            }

            this.dataValue = dataValue;
            return this;
        }

        /**
         * Changes the item's display name to the given value
         * @param name The new custom name for the item
         * @return A reference to self
         */
        public Builder withName(Component name) {

            name = ItemUtil.applyItemNameBaseStyle(name);
            String strName = version.hasFeature(GameVersion.Feature.ITEM_NAME_COMPONENTS) ?
                    name.toJSONString() :
                    name.toLegacyText();

            getOrCreateTag().getOrCreateSection("display").set("Name", strName);
            return this;
        }


        /**
         * Changes the item's lore to the given value
         * @param lore The new lore for the item
         * @return A reference to self
         */
        public Builder withLore(Collection<Component> lore) {

            List<String> loreList = lore.stream()
                .map(ItemUtil::applyItemLoreBaseStyle)
                .map(version.hasFeature(GameVersion.Feature.ITEM_NAME_COMPONENTS) ?
                        Component::toJSONString :
                        Component::toLegacyText
                    ).collect(Collectors.toList());

            getOrCreateTag().getOrCreateSection("display").set("Lore", ConfigList.of(loreList));
            return this;
        }


        public Builder withEnchantment(Identifier id, int level) {

            ConfigSection tag = getOrCreateTag();
            if(!tag.hasList("Enchantments")) {
                tag.set("Enchantments", new ConfigList());
            }

            tag.getList("Enchantments").add(new ConfigSection().with("id", id.toString()).with("lvl", level));
            return this;
        }

        public Builder withLegacyEnchantment(int id, int level) {

            ConfigSection tag = getOrCreateTag();
            if(!tag.hasList("ench")) {
                tag.set("ench", new ConfigList());
            }

            tag.getList("ench").add(new ConfigSection().with("id", id).with("lvl", level));
            return this;
        }

        /**
         * Makes an exact copy of this Builder
         * @return An exact copy of the Builder
         */
        public Builder copy() {
            Builder out = new Builder(version, id);
            out.count = count;
            out.tag = tag;
            out.dataValue = dataValue;
            return out;
        }

        /**
         * Constructs a new builder with the given type ID
         * @param id The type ID
         * @return A new builder
         */
        public static Builder of(Identifier id) {
            return new Builder(GameVersion.CURRENT_VERSION.get(), id);
        }

        /**
         * Constructs a new builder with the given type ID
         * @param version The game version to make the item for
         * @param id The type ID
         * @return A new builder
         */
        public static Builder of(GameVersion version, Identifier id) {
            return new Builder(version, id);
        }


        /**
         * Constructs a new builder for a specific color of the item with the given ID
         * @param color The color of item to build
         * @param base The base ID of the item (i.e. wool for green_wool, concrete for red_concrete, etc.)
         * @return A new builder
         */
        public static Builder ofColor(Color color, Identifier base) {
            return ofColor(GameVersion.CURRENT_VERSION.get(), color, base);
        }

        /**
         * Constructs a new builder for a specific color of the item with the given ID
         * @param version The game version to make the item for
         * @param color The color of item to build
         * @param base The base ID of the item (i.e. wool for green_wool, concrete for red_concrete, etc.)
         * @return A new builder
         */
        public static Builder ofColor(GameVersion version, Color color, Identifier base) {

            Builder out;
            if(version.hasFeature(GameVersion.Feature.NAMESPACED_IDS)) {
                out = new Builder(version,new Identifier(base.getNamespace(), TextColor.toDyeColor(color) + "_" + base.getPath()));
            } else {
                out = new Builder(version,base).withDataValue(TextColor.getLegacyDataValue(color));
            }

            return out;
        }

        /**
         * Constructs a new builder for a wool block with the given color
         * @param color The color of the wool block
         * @return A new builder
         */
        public static Builder woolWithColor(Color color) {
            return woolWithColor(GameVersion.CURRENT_VERSION.get(), color);
        }

        /**
         * Constructs a new builder for a wool block with the given color
         * @param version The game version to make the item for
         * @param color The color of the wool block
         * @return A new builder
         */
        public static Builder woolWithColor(GameVersion version, Color color) {
            return ofColor(version, color, new Identifier("minecraft", "wool"));
        }

        /**
         * Constructs a new builder for a stained glass block with the given color
         * @param color The color of the stained glass block
         * @return A new builder
         */
        public static Builder glassWithColor(Color color) {
            return glassWithColor(GameVersion.CURRENT_VERSION.get(), color);
        }

        /**
         * Constructs a new builder for a stained glass block with the given color
         * @param version The game version to make the item for
         * @param color The color of the stained glass block
         * @return A new builder
         */
        public static Builder glassWithColor(GameVersion version, Color color) {
            return ofColor(version, color, new Identifier("minecraft", "stained_glass"));
        }

        /**
         * Constructs a new builder for a stained glass pane block with the given color
         * @param color The color of the stained glass pane block
         * @return A new builder
         */
        public static Builder glassPaneWithColor(Color color) {
            return glassPaneWithColor(GameVersion.CURRENT_VERSION.get(), color);
        }

        /**
         * Constructs a new builder for a stained glass pane block with the given color
         * @param version The game version to make the item for
         * @param color The color of the stained glass pane block
         * @return A new builder
         */
        public static Builder glassPaneWithColor(GameVersion version, Color color) {
            return ofColor(version, color, new Identifier("minecraft", "stained_glass_pane"));
        }

        /**
         * Constructs a new builder for a concrete block with the given color
         * @param color The color of the concrete block
         * @return A new builder
         */
        public static Builder concreteWithColor(Color color) {
            return concreteWithColor(GameVersion.CURRENT_VERSION.get(), color);
        }

        /**
         * Constructs a new builder for a concrete block with the given color
         * @param version The game version to make the item for
         * @param color The color of the concrete block
         * @return A new builder
         */
        public static Builder concreteWithColor(GameVersion version, Color color) {
            return ofColor(version, color, new Identifier("minecraft", "concrete"));
        }

        /**
         * Constructs a new builder for a concrete powder block with the given color
         * @param color The color of the concrete powder block
         * @return A new builder
         */
        public static Builder concretePowderWithColor(Color color) {
            return concretePowderWithColor(GameVersion.CURRENT_VERSION.get(), color);
        }

        /**
         * Constructs a new builder for a concrete powder block with the given color
         * @param version The game version to make the item for
         * @param color The color of the concrete powder block
         * @return A new builder
         */
        public static Builder concretePowderWithColor(GameVersion version, Color color) {
            return ofColor(version, color, new Identifier("minecraft", "concrete_powder"));
        }

        /**
         * Constructs a new builder for a terracotta block with the given color
         * @param color The color of the terracotta block
         * @return A new builder
         */
        public static Builder terracottaWithColor(Color color) {
            return terracottaWithColor(GameVersion.CURRENT_VERSION.get(), color);
        }

        /**
         * Constructs a new builder for a terracotta block with the given color
         * @param version The game version to make the item for
         * @param color The color of the terracotta block
         * @return A new builder
         */
        public static Builder terracottaWithColor(GameVersion version, Color color) {

            String id = version.getProtocolVersion() >= 347 ? "terracotta" : "stained_hardened_clay";
            return ofColor(version, color, new Identifier("minecraft", id));
        }

        /**
         * Constructs a new builder for a player head with the given skin
         * @param skin The skin to apply to the player head item
         * @return A new builder
         */
        public static Builder headWithSkin(Skin skin) {
            return headWithSkin(GameVersion.CURRENT_VERSION.get(), skin);
        }

        /**
         * Constructs a new builder for a player head with the given skin
         * @param version The game version to make the item for
         * @param skin The skin to apply to the player head item
         * @return A new builder
         */
        public static Builder headWithSkin(GameVersion version, Skin skin) {

            Builder out;
            if(version.hasFeature(GameVersion.Feature.NAMESPACED_IDS)) {
                out = new Builder(version, new Identifier("minecraft", "player_head"));
            } else {
                out = new Builder(version, new Identifier("minecraft", "skull")).withDataValue((byte) 3);
            }

            ConfigObject uuid;
            if(version.hasFeature(GameVersion.Feature.INT_ARRAY_UUIDS)) {
                int[] parts = ItemUtil.splitUUID(skin.getUUID());
                uuid = new ConfigList().append(parts[0]).append(parts[1]).append(parts[2]).append(parts[3]);
            } else {
                uuid = new ConfigPrimitive(skin.getUUID().toString());
            }

            ConfigSection skullOwner = new ConfigSection()
                .with("Id", uuid)
                .with("Properties", new ConfigSection().with("textures", new ConfigList().append(new ConfigSection().with("Value", skin.getValue()))));

            out.getOrCreateTag().set("SkullOwner", skullOwner);
            return out;
        }

        /**
         * Constructs an ItemStack from the data in this builder
         * @return A new ItemStack
         */
        public ItemStack build() {
            return FACTORY.get().build(id, count, tag, dataValue, version);
        }
    }

}
