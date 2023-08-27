package org.wallentines.mcore.test;

import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.ItemStack;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

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
        private int count;
        private ConfigSection tag;
        private byte legacyData;

        public DummyItem(Identifier type, int count, ConfigSection tag, byte legacyData) {
            this.type = type;
            this.count = count;
            this.tag = tag;
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
        public ConfigSection getTag() {
            return tag;
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
        public void setTag(ConfigSection tag) {
            this.tag = tag;
        }

        @Override
        public void grow(int amount) {
            setCount(count + amount);
        }

        @Override
        public void shrink(int amount) {
            grow(-amount);
        }
    }


    public static final DynamicVersion VERSION = new DynamicVersion();

    static {

        GameVersion.CURRENT_VERSION.set(VERSION);
        ItemStack.FACTORY.set(DummyItem::new);
    }

}
