package me.m1dnightninja.midnightcore.spigot.inventory;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.spigot.util.ConversionUtil;
import me.m1dnightninja.midnightcore.spigot.util.NMSWrapper;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SpigotItem extends MItemStack {

    ItemStack stack;

    public SpigotItem(MIdentifier type, int count) {
        super(type, count);

        stack = ConversionUtil.toBukkitStack(this);
    }

    public SpigotItem(MIdentifier type, int count, ConfigSection tag) {
        super(type, count, tag);

        stack = ConversionUtil.toBukkitStack(this);
    }

    public SpigotItem(ItemStack is) {
        super(ConversionUtil.fromNamespacedKey(is.getType().getKey()), is.getAmount(), NMSWrapper.getItemTag(is));
        stack = is;
    }

    @Override
    public void update() {

        NMSWrapper.setItemTag(stack, tag);
        stack.setAmount(count);

    }

    @Override
    public MComponent getName() {

        ItemMeta im = stack.getItemMeta();
        if(im != null && im.hasDisplayName()) {
            return MComponent.Serializer.parse(im.getDisplayName());
        }

        NamespacedKey key = stack.getType().getKey();
        return MComponent.createTranslatableComponent((stack.getType().isBlock() ? "block" : "item") + "." + key.getNamespace() + "." + key.getKey());

    }

    public ItemStack getBukkitStack() {
        return stack;
    }


}
