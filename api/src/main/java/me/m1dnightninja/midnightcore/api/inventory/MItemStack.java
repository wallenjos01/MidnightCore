package me.m1dnightninja.midnightcore.api.inventory;

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

public class MItemStack {

    private final MIdentifier type;
    private int count;
    private ConfigSection tag;

    private MItemStack(MIdentifier type, int count) {
        this.type = type;
        this.count = count;
        this.tag = new ConfigSection();
    }

    private MItemStack(MIdentifier type, int count, ConfigSection tag) {
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

    public MItemStack copy() {

        return new MItemStack(type, count, tag);
    }

    public static class Builder {

        private final MIdentifier type;

        private int amount = 1;

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

            MItemStack is = new MItemStack(type, amount);

            if(name != null || lore != null) {
                ConfigSection display = new ConfigSection();

                if(name != null) {

                    display.set("Name", name);
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

                skullOwner.set("Id", UUIDtoInts(headSkin.getUUID()));

                ConfigSection properties = new ConfigSection();
                List<ConfigSection> textures = new ArrayList<>();

                ConfigSection property = new ConfigSection();
                property.set("Value", headSkin.getBase64());

                textures.add(property);
                properties.set("textures", textures);

                tag.set("SkullOwner", skullOwner);
                tag.set("Properties", properties);

            }

            return is;
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

            Builder builder = Builder.of(MIdentifier.parse(section.getString("type")));

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

            return builder.build();
        }

        @Override
        public ConfigSection serialize(MItemStack object) {

            ConfigSection out = new ConfigSection();

            out.set("type", object.type.toString());
            out.set("amount", object.count);
            out.set("tag", object.tag);

            return out;
        }
    };


}
