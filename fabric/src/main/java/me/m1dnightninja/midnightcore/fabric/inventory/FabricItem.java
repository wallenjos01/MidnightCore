package me.m1dnightninja.midnightcore.fabric.inventory;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.math.Color;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.MHoverEvent;
import me.m1dnightninja.midnightcore.api.text.MStyle;
import me.m1dnightninja.midnightcore.fabric.util.ConversionUtil;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FabricItem extends MItemStack {

    private final ItemStack stack;

    public FabricItem(ItemStack is) {

        super(ConversionUtil.fromResourceLocation(Registry.ITEM.getKey(is.getItem())), is.getCount());
        stack = is;

        tag = ConversionUtil.fromCompoundTag(is.getOrCreateTag());
    }

    public FabricItem(MIdentifier type, int count) {
        super(type, count);

        Item i = Registry.ITEM.get(ConversionUtil.toResourceLocation(type));
        stack = new ItemStack(i, count);
    }

    public FabricItem(MIdentifier type, int count, ConfigSection tag) {
        super(type, count, tag);

        Item i = Registry.ITEM.get(ConversionUtil.toResourceLocation(type));
        stack = new ItemStack(i, count);

        stack.setTag(ConversionUtil.toCompoundTag(tag));
    }

    @Override
    public void update() {

        stack.setTag(ConversionUtil.toCompoundTag(tag));
        stack.setCount(count);
    }

    @Override
    public MComponent getName() {

        MComponent cmp;
        if(stack.hasCustomHoverName()) {

            cmp = MComponent.createTranslatableComponent("").withStyle(new MStyle().withItalic(Boolean.TRUE));
            cmp.addChild(MComponent.Serializer.fromJson(tag.getSection("display").getString("Name")));

        } else {

            cmp = MComponent.createTranslatableComponent(stack.getItem().getDescriptionId());
        }

        if(stack.isEnchanted()) {
            cmp.getStyle().fill(new MStyle().withColor(Color.fromRGBI(11)));
        } else {
            Integer color = stack.getRarity().color.getColor();
            if(color != null) {
                cmp.getStyle().fill(new MStyle().withColor(new Color(color)));
            }
        }

        return cmp.withHoverEvent(MHoverEvent.createItemHover(this));
    }

    public ItemStack getMinecraftItem() {
        return stack;
    }
}
