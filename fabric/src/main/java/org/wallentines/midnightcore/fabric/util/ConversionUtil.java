package org.wallentines.midnightcore.fabric.util;

import com.google.gson.internal.LazilyParsedNumber;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightcore.api.text.*;
import org.wallentines.midnightcore.api.text.TextColor;
import org.wallentines.midnightcore.fabric.mixin.AccessorStyle;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
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

    public static MComponent toMComponent(@Nullable Component component) {

        if(component == null) return null;

        MComponent out;

        ComponentContents contents = component.getContents();

        if(contents instanceof TranslatableContents) {

            List<MComponent> comps = new ArrayList<>();
            for(Object o : ((TranslatableContents) component).getArgs()) {
                comps.add(toMComponent((Component) o));
            }

            out = new MTranslateComponent(((TranslatableContents) contents).getKey(), comps.isEmpty() ? null : comps);

        } else if(contents instanceof LiteralContents) {

            out = new MTextComponent(((LiteralContents) contents).text());

        } else {

            return MComponent.SERIALIZER.deserialize(GsonContext.INSTANCE, Component.Serializer.toJsonTree(component)).getOrThrow();
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
        if(style.isEmpty()) return out;

        AccessorStyle acc = (AccessorStyle) style;

        if(style.getColor() != null) out.withColor(TextColor.parse(style.getColor().serialize()));
        out.withBold(acc.getBold());
        out.withItalic(acc.getItalic());
        out.withUnderlined(acc.getUnderlined());
        out.withStrikethrough(acc.getStrikethrough());
        out.withObfuscated(acc.getObfuscated());

        out.withFont(toIdentifier(style.getFont()));

        return out;
    }

    public static MHoverEvent toMHoverEvent(HoverEvent event) {

        if(event == null) return null;

        String json = event.serialize().toString();
        ConfigObject obj = JSONCodec.minified().decode(ConfigContext.INSTANCE, json);

        return MHoverEvent.SERIALIZER.deserialize(ConfigContext.INSTANCE, obj).getOrThrow();
    }

    public static MClickEvent toMClickEvent(ClickEvent event) {

        if(event == null) return null;
        return new MClickEvent(MClickEvent.ClickAction.byId(event.getAction().getName()), event.getValue());
    }

    public static Component toComponent(@Nullable MComponent component) {

        if(component == null) return null;

        MutableComponent out;
        if(component instanceof MTextComponent) {

            out = Component.literal(component.getContent());

        } else if(component instanceof MTranslateComponent) {

            Collection<MComponent> args = ((MTranslateComponent) component).getArgs();
            Object[] comps = new Component[args.size()];

            int i = 0;
            for(MComponent arg : args) {
                comps[i++] = toComponent(arg);
            }

            out = Component.translatable(component.getContent(), comps);

        } else {

            return Component.Serializer.fromJson(JSONCodec.minified().encodeToString(ConfigContext.INSTANCE, MComponent.SERIALIZER.serialize(ConfigContext.INSTANCE, component).getOrThrow()));
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
        return HoverEvent.deserialize(MHoverEvent.SERIALIZER.serialize(GsonContext.INSTANCE, event).getOrThrow().getAsJsonObject());
    }

    public static ClickEvent toClickEvent(MClickEvent event) {

        if(event == null) return null;
        return new ClickEvent(ClickEvent.Action.getByName(event.getAction().getId()), event.getValue());
    }


    private static ConfigObject fromNBT(Tag t) {

        if(t instanceof CompoundTag) {

            ConfigSection sec = new ConfigSection();
            for(String s : ((CompoundTag) t).getAllKeys()) {

                sec.set(s, fromNBT(((CompoundTag) t).get(s)));
            }
            return sec;

        } else if(t instanceof IntTag) {

            return new ConfigPrimitive(((IntTag) t).getAsInt());

        } else if(t instanceof DoubleTag) {

            return new ConfigPrimitive(((DoubleTag) t).getAsDouble());

        } else if(t instanceof FloatTag) {

            return new ConfigPrimitive(((FloatTag) t).getAsFloat());

        } else if(t instanceof ShortTag) {

            return new ConfigPrimitive(((ShortTag) t).getAsShort());

        } else if(t instanceof ByteTag) {

            return new ConfigPrimitive(((ByteTag) t).getAsByte());

        } else if(t instanceof LongTag) {

            return new ConfigPrimitive(((LongTag) t).getAsLong());

        } else if(t instanceof StringTag) {

            return new ConfigPrimitive(t.getAsString());

        } else if(t instanceof ListTag lt) {

            ConfigList objs = new ConfigList();
            for(Tag t1 : lt) {

                objs.add(fromNBT(t1));
            }
            return objs;

        } else if(t instanceof IntArrayTag lt) {

            ConfigList objs = new ConfigList();
            for(IntTag t1 : lt) {

                objs.add(t1.getAsInt());
            }
            return objs;

        } else if(t instanceof LongArrayTag lt) {

            ConfigList objs = new ConfigList();
            for(LongTag t1 : lt) {

                objs.add(t1.getAsLong());
            }
            return objs;

        } else if(t instanceof ByteArrayTag lt) {

            ConfigList objs = new ConfigList();
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
