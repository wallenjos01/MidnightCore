package me.m1dnightninja.midnightcore.api.inventory;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.config.ConfigSerializer;
import me.m1dnightninja.midnightcore.api.math.Color;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.MStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public abstract class MItemStack {

    protected final MIdentifier type;
    protected int count;
    protected ConfigSection tag;

    protected byte data;

    protected MItemStack(MIdentifier type, int count) {
        this.type = type;
        this.count = count;
        this.tag = new ConfigSection();
    }

    protected MItemStack(MIdentifier type, int count, ConfigSection tag) {
        this.type = type;
        this.count = count;
        this.tag = tag;
    }

    public MIdentifier getType() {
        return type;
    }

    public int getCount() {
        return count;
    }

    public ConfigSection getTag() {
        return tag;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setTag(ConfigSection tag) {
        this.tag = tag;
    }

    public abstract void update();

    public abstract MComponent getName();

    public void setName(MComponent component) {

        tag.getOrCreateSection("display").set("Name", MComponent.Serializer.toJsonString(component));
    }

    public MItemStack copy() {

        return Builder.of(type).withAmount(count).withTag(tag).build();
    }

    public static class Builder {

        private final MIdentifier type;

        private int amount = 1;
        private final int majorVersion = MidnightCoreAPI.getInstance().getGameMajorVersion();

        private Skin headSkin = null;
        private MComponent name = null;
        private Iterable<MComponent> lore = null;

        private ConfigSection tag = new ConfigSection();

        private Builder(MIdentifier type) {
            this.type = type;
        }

        public Builder withAmount(int amount) {
            this.amount = amount;
            return this;
        }

        public Builder withName(MComponent name) {
            name.getStyle().fill(MStyle.ITEM_BASE);
            this.name = name;
            return this;
        }

        public Builder withNameRaw(MComponent name) {
            this.name = name;
            return this;
        }

        public Builder withLore(Iterable<MComponent> lore) {
            this.lore = lore;
            return this;
        }

        public Builder withTag(ConfigSection sec) {
            tag = sec;
            return this;
        }

        public MItemStack build() {

            if(name != null || lore != null) {
                ConfigSection display = new ConfigSection();

                if(name != null) {

                    String outName;
                    if (majorVersion <= 12) {
                        outName = "§r" + name.toLegacyText('§', null);
                    } else {
                        outName = MComponent.Serializer.toJsonString(name);
                    }

                    display.set("Name", outName);
                }

                if(lore != null) {

                    List<String> listLore = new ArrayList<>();
                    for(MComponent cmp : lore) {
                        listLore.add(MComponent.Serializer.toJsonString(cmp));
                    }

                    display.set("Lore", listLore);
                }

                tag.set("display", display);
            }

            if(headSkin != null) {

                ConfigSection skullOwner = new ConfigSection();

                skullOwner.set("Id", majorVersion > 15 ? UUIDtoInts(headSkin.getUUID()) : headSkin.getUUID().toString());

                ConfigSection properties = new ConfigSection();
                List<ConfigSection> textures = new ArrayList<>();

                ConfigSection property = new ConfigSection();
                property.set("Value", headSkin.getBase64());

                textures.add(property);
                properties.set("textures", textures);

                skullOwner.set("Properties", properties);
                tag.set("SkullOwner", skullOwner);
            }

            return MidnightCoreAPI.getInstance().getItemConverter().createItem(type, amount, tag);
        }


        public static Builder of(MIdentifier type) {
            return new Builder(type);
        }

        public static Builder woolWithColor(Color color) {

            String colorName = color.toDyeColor();

            return new Builder(MIdentifier.create("minecraft", colorName + "_wool"));

        }

        public static Builder headWithSkin(Skin skin) {

            Builder out = new Builder(MIdentifier.create("minecraft", "player_head"));
            out.headSkin = skin;

            return out;

        }

        private static List<Integer> UUIDtoInts(UUID u) {
            long u1 = u.getMostSignificantBits();
            long u2 = u.getLeastSignificantBits();
            return Arrays.asList((int) (u1 >> 32), (int) u1, (int) (u2 >> 32), (int) u2);
        }

    }

    public static final ConfigSerializer<MItemStack> SERIALIZER = new ConfigSerializer<MItemStack>() {
        @Override
        public MItemStack deserialize(ConfigSection section) {

            Builder builder = Builder.of(MIdentifier.parseOrDefault(section.getString("type")));

            if(section.has("name", String.class)) {
                builder.withName(MComponent.Serializer.parse(section.getString("name")));
            }

            if(section.has("lore", List.class)) {

                List<MComponent> comp = new ArrayList<>();
                for(String s : section.getStringList("lore")) {
                    comp.add(MComponent.Serializer.parse(s));
                }

                builder.withLore(comp);
            }

            if(section.has("amount", Integer.class)) {

                builder.withAmount(section.getInt("amount"));
            }

            if(section.has("tag", ConfigSection.class)) {

                builder.withTag(section.getSection("tag"));
            }

            MItemStack is = builder.build();

            if(MidnightCoreAPI.getInstance().getGameMajorVersion() < 13 && section.has("data", Number.class)) {
                is.data = section.get("data", Number.class).byteValue();
                is.update();
            }

            return is;
        }

        @Override
        public ConfigSection serialize(MItemStack object) {

            ConfigSection out = new ConfigSection();

            out.set("type", MidnightCoreAPI.getInstance().getGameMajorVersion() >= 13 ? object.type.toString() : object.type.getPath());
            out.set("amount", object.count);
            out.set("tag", object.tag);

            if(MidnightCoreAPI.getInstance().getGameMajorVersion() < 13 && object.data > 0) {
                out.set("data", object.data);
            }

            return out;
        }
    };


}
