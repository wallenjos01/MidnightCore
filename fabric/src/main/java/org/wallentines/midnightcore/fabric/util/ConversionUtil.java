package org.wallentines.midnightcore.fabric.util;

import com.google.gson.internal.LazilyParsedNumber;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import org.wallentines.midnightcore.api.text.*;
import org.wallentines.midnightcore.api.text.TextColor;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ConversionUtil {

    public static Identifier toIdentifier(ResourceLocation loc) {
        if(loc == null) return null;
        return new Identifier(loc.getNamespace(), loc.getPath());
    }

    public static ResourceLocation toResourceLocation(Identifier id) {
        if(id == null) return null;
        return new ResourceLocation(id.getNamespace(), id.getPath());
    }

    public static ConfigSection toConfigSection(CompoundTag tag) {

        if(tag == null) return null;

        ConfigSection out = new ConfigSection();
        for(String s : tag.getAllKeys()) {

            Tag o = tag.get(s);
            out.set(s, fromNBT(o));
        }

        return out;
    }

    public static CompoundTag toCompoundTag(ConfigSection section) {

        CompoundTag out = new CompoundTag();

        for(String s : section.getKeys()) {

            Object o = section.get(s);
            out.put(s, toNBT(o));
        }

        return out;
    }

    public static MComponent toMComponent(Component component) {

        MComponent out;
        if(component instanceof TranslatableComponent) {

            List<MComponent> comps = new ArrayList<>();
            for(Object o : ((TranslatableComponent) component).getArgs()) {
                comps.add(toMComponent((Component) o));
            }

            out = new MTranslateComponent(((TranslatableComponent) component).getKey(), comps.isEmpty() ? null : comps);

        } else if(component instanceof TextComponent) {

            out = new MTextComponent(((TextComponent) component).getText());

        } else {

            return MComponent.SERIALIZER.deserialize(JsonConfigProvider.INSTANCE.loadFromString(Component.Serializer.toJson(component)));
        }

        out.withStyle(toMStyle(component.getStyle()));
        out.setHoverEvent(toMHoverEvent(component.getStyle().getHoverEvent()));
        out.setClickEvent(toMClickEvent(component.getStyle().getClickEvent()));

        for(Component cmp : component.getSiblings()) {
            out.addChild(toMComponent(cmp));
        }
        return out;
    }

    public static MStyle toMStyle(Style style) {

        MStyle out = new MStyle();
        if(style.getColor() != null) out.withColor(TextColor.parse(style.getColor().serialize()));

        String styleStr = style.toString().substring(7);
        StringBuilder[] builders = new StringBuilder[2];
        builders[0] = new StringBuilder();
        builders[1] = new StringBuilder();

        int index = 0;

        for(char c : styleStr.toCharArray()) {
            if(c == ',') {

                String key = builders[0].toString();
                String value = builders[1].toString();

                Boolean val = value.equals("null") ? null : Boolean.valueOf(value);

                switch (key) {
                    case "bold" -> out.withBold(val);
                    case "italic" -> out.withItalic(val);
                    case "underlined" -> out.withUnderlined(val);
                    case "strikethrough" -> out.withStrikethrough(val);
                    case "obfuscated" -> out.withObfuscated(val);
                    case "font" -> out.withFont(val == null || value.equals("minecraft:default") ? null : Identifier.parse(value));
                }

                builders[0] = new StringBuilder();
                builders[1] = new StringBuilder();

            } else if(c == '=') {

                index = (index + 1) % 2;

            } else if(c != ' ') {

                builders[index].append(c);

            }
        }

        return out;
    }

    public static MHoverEvent toMHoverEvent(HoverEvent event) {

        if(event == null) return null;
        return MHoverEvent.SERIALIZER.deserialize(JsonConfigProvider.INSTANCE.loadFromString(event.serialize().toString()));
    }

    public static MClickEvent toMClickEvent(ClickEvent event) {

        if(event == null) return null;
        return new MClickEvent(MClickEvent.ClickAction.getById(event.getAction().getName()), event.getValue());
    }

    public static Component toComponent(MComponent component) {

        MutableComponent out;
        if(component instanceof MTextComponent) {

            out = new TextComponent(component.getContent());

        } else if(component instanceof MTranslateComponent) {

            List<MComponent> data = ((MTranslateComponent) component).getArgs();
            Object[] comps = new Component[data.size()];

            for(int i = 0 ; i < comps.length ; i ++) {
                comps[i] = toComponent(data.get(i));
            }

            out = new TranslatableComponent(component.getContent(), comps);

        } else {

            return Component.Serializer.fromJson(MComponent.SERIALIZER.serialize(component).toJson());
        }

        out.setStyle(toStyle(component.getStyle())
                .withInsertion(component.getInsertion())
                .withHoverEvent(toHoverEvent(component.getHoverEvent()))
                .withClickEvent(toClickEvent(component.getClickEvent()))
        );

        for(MComponent child : component.getChildren()) {
            out.append(toComponent(child));
        }

        return out;
    }

    public static Style toStyle(MStyle style) {

        if(style == null) return Style.EMPTY;
        TextColor color = style.getColor();

        return Style.EMPTY
                .withColor(color == null ? null : net.minecraft.network.chat.TextColor.fromRgb(color.toDecimal()))
                .withBold(style.getBold())
                .withItalic(style.getItalic())
                .withUnderlined(style.getUnderlined())
                .withStrikethrough(style.getStrikethrough())
                .withObfuscated(style.getObfuscated())
                .withFont(toResourceLocation(style.getFont()));
    }

    public static HoverEvent toHoverEvent(MHoverEvent event) {

        if(event == null) return null;
        return HoverEvent.deserialize(MHoverEvent.SERIALIZER.serialize(event).toJson());
    }

    public static ClickEvent toClickEvent(MClickEvent event) {

        if(event == null) return null;
        return new ClickEvent(ClickEvent.Action.getByName(event.getAction().getId()), event.getValue());
    }


    private static Object fromNBT(Tag t) {

        if(t instanceof CompoundTag) {

            ConfigSection sec = new ConfigSection();
            for(String s : ((CompoundTag) t).getAllKeys()) {

                sec.set(s, fromNBT(((CompoundTag) t).get(s)));
            }
            return sec;

        } else if(t instanceof IntTag) {

            return ((IntTag) t).getAsInt();

        } else if(t instanceof DoubleTag) {

            return ((DoubleTag) t).getAsDouble();

        } else if(t instanceof FloatTag) {

            return ((FloatTag) t).getAsFloat();

        } else if(t instanceof ShortTag) {

            return ((ShortTag) t).getAsShort();

        } else if(t instanceof ByteTag) {

            return ((ByteTag) t).getAsByte();

        } else if(t instanceof LongTag) {

            return ((LongTag) t).getAsLong();

        } else if(t instanceof StringTag) {

            return t.getAsString();

        } else if(t instanceof ListTag lt) {

            List<Object> objs = new ArrayList<>();
            for(Tag t1 : lt) {

                objs.add(fromNBT(t1));
            }
            return objs;

        } else if(t instanceof IntArrayTag lt) {

            List<Integer> objs = new ArrayList<>();
            for(IntTag t1 : lt) {

                objs.add(t1.getAsInt());
            }
            return objs;

        } else if(t instanceof LongArrayTag lt) {

            List<Long> objs = new ArrayList<>();
            for(LongTag t1 : lt) {

                objs.add(t1.getAsLong());
            }
            return objs;

        } else if(t instanceof ByteArrayTag lt) {

            List<Byte> objs = new ArrayList<>();
            for(ByteTag t1 : lt) {

                objs.add(t1.getAsByte());
            }
            return objs;
        }

        return null;
    }

    private static Tag toNBT(Object o) {

        if(o instanceof ConfigSection) {
            return toCompoundTag((ConfigSection) o);

        } else if(o instanceof LazilyParsedNumber num) {

            String s = num.toString();
            if(s.contains(".")) {
                return DoubleTag.valueOf(num.doubleValue());
            } else {
                return IntTag.valueOf(num.intValue());
            }

        } else if(o instanceof Integer) {
            return IntTag.valueOf((int) o);

        } else if(o instanceof Double) {
            return DoubleTag.valueOf((double) o);

        } else if(o instanceof Float) {
            return FloatTag.valueOf((float) o);

        } else if(o instanceof Short) {
            return ShortTag.valueOf((short) o);

        } else if(o instanceof Byte) {
            return ByteTag.valueOf((byte) o);

        } else if(o instanceof Long) {
            return LongTag.valueOf((long) o);

        } else if(o instanceof String) {
            return StringTag.valueOf(o.toString());

        } else if(o instanceof Boolean) {
            return (Boolean) o ? ByteTag.ONE : ByteTag.ZERO;

        } else if(o instanceof List<?> l) {

            if (l.size() == 0) {
                return new ListTag();
            }

            Object l1 = ((List<?>) o).get(0);
            if (l1 instanceof Number) {

                int[] values = new int[l.size()];

                for (int i = 0; i < values.length; i++) {
                    values[i] = ((Number) l.get(i)).intValue();
                }

                return new IntArrayTag(values);

            } else {

                ListTag t = new ListTag();

                for(Object lv : l) {
                    Tag tag = toNBT(lv);
                    if(tag == null) continue;

                    t.add(tag);
                }

                return t;
            }
        }

        return StringTag.valueOf(o.toString());
    }

}
