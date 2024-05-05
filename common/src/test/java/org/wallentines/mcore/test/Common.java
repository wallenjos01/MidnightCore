package org.wallentines.mcore.test;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.ItemStack;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.stream.Stream;

public class Common {


    // Cursed class to change protocol version at runtime, because tests all run in the same execution
    public static class DynamicVersion extends GameVersion {

        private String id;
        private int protocolVersion;

        public DynamicVersion() {
            super("null", -1);
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public int getProtocolVersion() {
            return protocolVersion;
        }

        @Override
        public boolean hasFeature(Feature feature) {
            return feature.check(protocolVersion);
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setProtocolVersion(int protocolVersion) {
            this.protocolVersion = protocolVersion;
        }
    }

    public static class DummyItem implements ItemStack {

        private final Identifier type;
        private final byte legacyData;
        private int count;
        private ConfigSection customData;

        public DummyItem(Identifier type, int count, ConfigSection customData, byte legacyData) {
            this.type = type;
            this.count = count;
            this.customData = customData;
            this.legacyData = legacyData;
        }

        @Override
        public Identifier getType() {
            return type;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public byte getLegacyDataValue() {
            return legacyData;
        }

        @Override
        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public void loadComponent(Identifier id, ConfigObject config) {
            
        }

        @Override
        public ConfigObject saveComponent(Identifier id) {
            return null;
        }

        @Override
        public void removeComponent(Identifier id) {

        }

        @Override
        public @Nullable ConfigSection getCustomData() {
            return customData;
        }

        @Override
        public void setCustomData(ConfigSection section) {
            this.customData = section;
        }

        @Override
        public void fillCustomData(ConfigSection section) {
            customData.fillOverwrite(section);
        }

        @Override
        public Stream<Identifier> getComponentIds() {
            return null;
        }

        @Override
        public void grow(int amount) {
            setCount(count + amount);
        }

        @Override
        public void shrink(int amount) {
            grow(-amount);
        }

        @Override
        public String getTranslationKey() {
            return null;
        }

        @Override
        public GameVersion getVersion() {
            return Common.VERSION;
        }

        @Override
        public Color getRarityColor() {
            return Color.WHITE;
        }
    }


    public static final DynamicVersion VERSION = new DynamicVersion();

    static {

        GameVersion.CURRENT_VERSION.set(VERSION);
        ItemStack.FACTORY.set(new ItemStack.Factory() {
            @Override
            public ItemStack buildLegacy(Identifier id, int count, byte damage, ConfigSection tag, GameVersion version) {
                return new DummyItem(id,count,tag,damage);
            }

            @Override
            public ItemStack buildTagged(Identifier id, int count, ConfigSection tag, GameVersion version) {
                return new DummyItem(id,count,tag,(byte)0);
            }

            @Override
            public ItemStack buildStructured(Identifier id, int count, ItemStack.ComponentPatchSet components, GameVersion version) {
                return new DummyItem(id,count,null,(byte)0);
            }
        });
    }

}
