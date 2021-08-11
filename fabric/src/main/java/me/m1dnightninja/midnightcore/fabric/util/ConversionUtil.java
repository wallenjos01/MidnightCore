package me.m1dnightninja.midnightcore.fabric.util;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.math.Color;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.MHoverEvent;
import me.m1dnightninja.midnightcore.api.text.MStyle;
import me.m1dnightninja.midnightcore.fabric.inventory.FabricItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ConversionUtil {

    public static ResourceLocation toResourceLocation(MIdentifier id) {
        return new ResourceLocation(id.getNamespace(), id.getPath());
    }

    public static MIdentifier fromResourceLocation(ResourceLocation loc) {
        return MIdentifier.create(loc.getNamespace(), loc.getPath());
    }

    public static CompoundTag toCompoundTag(ConfigSection sec) {

        CompoundTag out = new CompoundTag();

        for(String s : sec.getKeys()) {

            Object o = sec.get(s);
            out.put(s, toNBT(o));
        }

        return out;
    }

    public static ConfigSection fromCompoundTag(CompoundTag tag) {

        ConfigSection out = new ConfigSection();
        for(String s : tag.getAllKeys()) {

            Tag o = tag.get(s);
            out.set(s, fromNBT(o));
        }

        return out;
    }

    public static ItemStack toMinecraftStack(MItemStack stack) {

        return ((FabricItem) stack).getMinecraftItem();
    }

    public static Component toMinecraftComponent(MComponent component) {

        MutableComponent out;
        if(component.getType() == MComponent.Type.TEXT) {

            out = new TextComponent(component.getContent());

        } else if(component.getType() == MComponent.Type.TRANSLATABLE) {

            int size = component.getTranslateData().size();
            if(size > 0) {

                Object[] data = new Component[size];
                for (int i = 0; i < size ; i++) {
                    data[i] = toMinecraftComponent(component.getTranslateData().get(i));
                }

                out = new TranslatableComponent(component.getContent(), data);

            } else {

                out = new TranslatableComponent(component.getContent());
            }
        } else {
            return null;
        }

        out = out.setStyle(toMinecraftStyle(component.getStyle(), component.getHoverEvent()));

        for(MComponent cmp : component.getChildren()) {
            out.append(toMinecraftComponent(cmp));
        }

        return out;
    }

    public static MComponent fromMinecraftComponent(Component cmp) {
        return MComponent.Serializer.fromJson(Component.Serializer.toJson(cmp));
    }

    private static Style toMinecraftStyle(MStyle style, MHoverEvent hoverEvent) {

        Style out = Style.EMPTY;

        out = out.withBold(style.isBold());
        out = out.withItalic(style.isItalic());

        if(style.getColor() != null) {
            out = out.withColor(toTextColor(style.getColor()));
        }
        if(style.getFont() != null) {
            out = out.withFont(toResourceLocation(style.getFont()));
        }
        if(style.isStrikethrough() == Boolean.TRUE) {
            out = out.applyFormat(ChatFormatting.STRIKETHROUGH);
        }
        if(style.isObfuscated() == Boolean.TRUE) {
            out = out.applyFormat(ChatFormatting.OBFUSCATED);
        }
        if(style.isUnderlined() == Boolean.TRUE) {
            out = out.applyFormat(ChatFormatting.UNDERLINE);
        }

        if(hoverEvent != null) {

            HoverEvent event = HoverEvent.deserialize(hoverEvent.toJson());
            out = out.withHoverEvent(event);

        }

        return out;
    }

    private static TextColor toTextColor(Color color) {
        return TextColor.fromRgb(color.toDecimal());
    }

    private static Color toColor(TextColor color) {
        return Color.parse(color.serialize());
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

        } else if(t instanceof ListTag) {

            ListTag lt = (ListTag) t;

            List<Object> objs = new ArrayList<>();
            for(Tag t1 : lt) {

                objs.add(fromNBT(t1));
            }
            return objs;

        } else if(t instanceof IntArrayTag) {

            IntArrayTag lt = (IntArrayTag)  t;

            List<Integer> objs = new ArrayList<>();
            for(IntTag t1 : lt) {

                objs.add(t1.getAsInt());
            }
            return objs;

        } else if(t instanceof LongArrayTag) {

            LongArrayTag lt = (LongArrayTag) t;

            List<Long> objs = new ArrayList<>();
            for(LongTag t1 : lt) {

                objs.add(t1.getAsLong());
            }
            return objs;

        } else if(t instanceof ByteArrayTag) {

            ByteArrayTag lt = (ByteArrayTag) t;

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

        } else if(o instanceof List<?>) {

            List<?> l = (List<?>) o;

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

        return null;
    }


}
