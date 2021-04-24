package me.m1dnightninja.midnightcore.fabric.util;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.math.Color;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.MStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ConversionUtil {

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

    public static ItemStack toMinecraftStack(MItemStack stack) {

        Item i = Registry.ITEM.get(toResourceLocation(stack.getType()));
        ItemStack out = new ItemStack(i, stack.getCount());

        out.setTag(toCompoundTag(stack.getTag()));
        return out;
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

        out = out.setStyle(toMinecraftStyle(component.getStyle()));

        for(MComponent cmp : component.getChildren()) {
            out.append(toMinecraftComponent(cmp));
        }

        return out;
    }

    private static Style toMinecraftStyle(MStyle style) {

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

        return out;
    }

    private static TextColor toTextColor(Color color) {
        return TextColor.fromRgb(color.toDecimal());
    }

    private static Color toColor(TextColor color) {
        return Color.parse(color.serialize());
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

        } else if(o instanceof String) {
            return StringTag.valueOf(o.toString());

        } else if(o instanceof List) {
            List<?> l = (List<?>) o;

            if (l.size() == 0) {
                return new ListTag();
            }

            Object l1 = ((List<?>) o).get(0);
            if (l1 instanceof Integer) {

                int[] values = new int[l.size()];

                for (int i = 0; i < values.length; i++) {
                    values[i] = (int) l.get(i);
                }

                return new IntArrayTag(values);

            } else if(l1 instanceof Long) {

                long[] values = new long[l.size()];
                for (int i = 0; i < values.length; i++) {
                    values[i] = (long) l.get(i);
                }

                return new LongArrayTag(values);

            } else if(l1 instanceof Byte) {

                byte[] values = new byte[l.size()];
                for (int i = 0; i < values.length; i++) {
                    values[i] = (byte) l.get(i);
                }

                return new ByteArrayTag(values);

            } else {

                ListTag t = new ListTag();

                for(Object lv : l) {
                    t.add(toNBT(lv));
                }

                return t;
            }
        }

        return null;
    }


}
