package org.wallentines.midnightcore.api.item;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MStyle;
import org.wallentines.midnightcore.api.text.TextColor;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public interface MItemStack {

    Identifier getType();

    int getCount();

    ConfigSection getTag();

    void setCount(int count);

    void setTag(ConfigSection tag);

    MComponent getName();

    void setName(MComponent component);

    List<MComponent> getLore();

    void setLore(List<MComponent> lore);

    MItemStack copy();

    String saveToNBT();

    void update();

    class Builder {

        private final Identifier type;

        private int amount = 1;
        private final int majorVersion = MidnightCoreAPI.getInstance().getGameVersion().getMinorVersion();

        private Skin headSkin = null;
        private MComponent name = null;
        private List<MComponent> lore = null;

        private ConfigSection tag = new ConfigSection();

        private Builder(Identifier type) {
            this.type = type;
        }

        public Builder withAmount(int amount) {
            this.amount = amount;
            return this;
        }

        public Builder withName(MComponent name) {

            name = name.copy();
            name.getStyle().fillFrom(MStyle.ITEM_NAME_BASE);

            this.name = name;
            return this;
        }

        public Builder withLore(Iterable<MComponent> lore) {
            this.lore = new ArrayList<>();
            for(MComponent comp : lore) {

                MComponent line = comp.copy();
                line.getStyle().fillFrom(MStyle.ITEM_LORE_BASE);

                this.lore.add(line);
            }
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

                    display.set("Name", name.toString());
                }

                if(lore != null) {

                    List<String> listLore = new ArrayList<>();
                    for(MComponent cmp : lore) {
                        listLore.add(cmp.toString());
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
                property.set("Value", headSkin.getValue());

                textures.add(property);
                properties.set("textures", textures);

                skullOwner.set("Properties", properties);
                tag.set("SkullOwner", skullOwner);
            }

            return MidnightCoreAPI.getInstance().createItem(type, amount, tag);
        }


        public static Builder of(Identifier type) {
            return new Builder(type);
        }

        public static Builder woolWithColor(TextColor color) {

            String colorName = color.toDyeColor();

            return new Builder(new Identifier("minecraft", colorName + "_wool"));

        }

        public static Builder headWithSkin(Skin skin) {

            Builder out = new Builder(new Identifier("minecraft", "player_head"));
            out.headSkin = skin;

            return out;

        }

        private static List<Integer> UUIDtoInts(UUID u) {
            long u1 = u.getMostSignificantBits();
            long u2 = u.getLeastSignificantBits();
            return Arrays.asList((int) (u1 >> 32), (int) u1, (int) (u2 >> 32), (int) u2);
        }
    }

    static String toNBT(ConfigSection section) {

        StringBuilder builder = new StringBuilder("{");

        List<String> keys = new ArrayList<>();

        for (String s : section.getKeys()) {
            keys.add(s);
        }

        for (int i = 0; i < keys.size(); i++) {

            if (i > 0) {
                builder.append(",");
            }

            String s = keys.get(i);
            Object o = section.get(s);

            builder.append(s).append(":").append(toNBTString(o));
        }

        builder.append("}");
        return builder.toString();
    }

    private static String toNBTString(Object o) {

        StringBuilder builder = new StringBuilder();

        if(o instanceof ConfigSection) {

            builder.append(toNBT((ConfigSection) o));

        } else if(o instanceof List<?>) {

            builder.append("[");

            int ints = 0;

            List<?> l = (List<?>) o;
            for(Object obj : l) {
                if(obj instanceof Number) ints++;
            }

            boolean intArray = ints == l.size() - 1 && "I".equals(l.get(0));

            if(ints == l.size() || intArray) {
                builder.append("I;");
            }

            int start = intArray ? 1 : 0;
            for(int i = start ; i < l.size() ; i++) {
                if(i > start) {
                    builder.append(",");
                }
                builder.append(toNBTString(l.get(i)));
            }

            builder.append("]");

        } else if(o instanceof String) {

            builder.append("\"");
            char prev = 0;
            for(char c : ((String) o).toCharArray()) {

                if(c == '"' && prev != '\\') {

                    builder.append("\\\"");

                } else {

                    builder.append(c);
                }
                prev = c;
            }
            builder.append("\"");

        } else {

            builder.append(o.toString());
        }

        return builder.toString();
    }



}
