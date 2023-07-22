package org.wallentines.mcore.item;

import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.TextColor;
import org.wallentines.mcore.util.ItemUtil;
import org.wallentines.mcore.util.Singleton;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.NumberSerializer;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Collection;
import java.util.List;

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
     * Will contain an item factory by the time the game loads
     */
    Singleton<Factory> FACTORY = new Singleton<>();

    Serializer<ItemStack> SERIALIZER = ObjectSerializer.create(
            Identifier.serializer("minecraft").entry("type", ItemStack::getType),
            NumberSerializer.forInt(1, 64).entry("count", ItemStack::getCount).orElse(1),
            ConfigSection.SERIALIZER.entry("tag", ItemStack::getTag).optional(),
            Serializer.BYTE.entry("data", ItemStack::getLegacyDataValue).orElse((byte) -1),
            (type, count, tag, data) -> FACTORY.get().build(type, count, tag, data)
    );


    /**
     * An interface to construct an item stack
     */
    interface Factory {
        ItemStack build(Identifier type, int count, ConfigSection tag, byte legacyData);
    }


    /**
     * A builder type to easily create item stacks.
     */
    class Builder {

        private final Identifier id;
        private int count = 1;
        private ConfigSection tag = null;
        private byte dataValue = -1;

        private Builder(Identifier id) {
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
            if(GameVersion.CURRENT_VERSION.get().hasFeature(GameVersion.Feature.NAMESPACED_IDS)) {
                throw new IllegalArgumentException("ItemStack data value specified on a version which does not support it!");
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
            String strName = GameVersion.CURRENT_VERSION.get().hasFeature(GameVersion.Feature.ITEM_NAME_COMPONENTS) ?
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
                .map(GameVersion.CURRENT_VERSION.get().hasFeature(GameVersion.Feature.ITEM_NAME_COMPONENTS) ?
                        Component::toJSONString :
                        Component::toLegacyText
                    ).toList();

            getOrCreateTag().getOrCreateSection("display").set("Lore", ConfigList.of(loreList));
            return this;
        }

        /**
         * Constructs a new builder with the given type ID
         * @param id The type ID
         * @return A new builder
         */
        public static Builder of(Identifier id) {
            return new Builder(id);
        }

        /**
         * Constructs a new builder for a specific color of the item with the given ID
         * @param color The color of item to build
         * @param base The base ID of the item (i.e. wool for green_wool, concrete for red_concrete, etc.)
         * @return A new builder
         */
        public static Builder ofColor(Color color, Identifier base) {

            Builder out;
            if(GameVersion.CURRENT_VERSION.get().hasFeature(GameVersion.Feature.NAMESPACED_IDS)) {
                out = new Builder(new Identifier(base.getNamespace(), "%s_%s".formatted(TextColor.toDyeColor(color), base.getPath())));
            } else {
                out = new Builder(base).withDataValue(TextColor.getLegacyDataValue(color));
            }

            return out;
        }

        /**
         * Constructs a new builder for a wool block with the given color
         * @param color The color of the wool block
         * @return A new builder
         */
        public static Builder woolWithColor(Color color) {
            return ofColor(color, new Identifier("minecraft", "wool"));
        }

        /**
         * Constructs a new builder for a stained glass block with the given color
         * @param color The color of the stained glass block
         * @return A new builder
         */
        public static Builder glassWithColor(Color color) {
            return ofColor(color, new Identifier("minecraft", "stained_glass"));
        }

        /**
         * Constructs a new builder for a stained glass pane block with the given color
         * @param color The color of the stained glass pane block
         * @return A new builder
         */
        public static Builder glassPaneWithColor(Color color) {
            return ofColor(color, new Identifier("minecraft", "stained_glass_pane"));
        }

        /**
         * Constructs a new builder for a concrete block with the given color
         * @param color The color of the concrete block
         * @return A new builder
         */
        public static Builder concreteWithColor(Color color) {
            return ofColor(color, new Identifier("minecraft", "concrete"));
        }

        /**
         * Constructs a new builder for a concrete powder block with the given color
         * @param color The color of the concrete powder block
         * @return A new builder
         */
        public static Builder concretePowderWithColor(Color color) {
            return ofColor(color, new Identifier("minecraft", "concrete_powder"));
        }

        /**
         * Constructs a new builder for a terracotta block with the given color
         * @param color The color of the terracotta block
         * @return A new builder
         */
        public static Builder terracottaWithColor(Color color) {

            String id = GameVersion.CURRENT_VERSION.get().getProtocolVersion() >= 347 ? "terracotta" : "stained_hardened_clay";
            return ofColor(color, new Identifier("minecraft", id));
        }

        /**
         * Constructs a new builder for a player head with the given skin
         * @param skin The skin to apply to the player head item
         * @return A new builder
         */
        public static Builder headWithSkin(Skin skin) {

            Builder out;
            if(GameVersion.CURRENT_VERSION.get().hasFeature(GameVersion.Feature.NAMESPACED_IDS)) {
                out = new Builder(new Identifier("minecraft", "player_head"));
            } else {
                out = new Builder(new Identifier("minecraft", "skull")).withDataValue((byte) 3);
            }

            ConfigObject uuid;
            if(GameVersion.CURRENT_VERSION.get().hasFeature(GameVersion.Feature.INT_ARRAY_UUIDS)) {
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
            return FACTORY.get().build(id, count, tag, dataValue);
        }
    }

}
