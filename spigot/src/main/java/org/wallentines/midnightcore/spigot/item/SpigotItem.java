package org.wallentines.midnightcore.spigot.item;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MTranslateComponent;
import org.wallentines.midnightcore.common.item.AbstractItem;
import org.wallentines.midnightcore.spigot.adapter.AdapterManager;
import org.wallentines.midnightcore.spigot.util.ConversionUtil;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Locale;

public class SpigotItem extends AbstractItem {

    private final ItemStack internal;

    public SpigotItem(ItemStack is) {
        super(ConversionUtil.toIdentifier(is.getType().getKey()), is.getAmount(), AdapterManager.getAdapter().getTag(is));
        internal = is;
    }

    public SpigotItem(Identifier typeId, int count, ConfigSection tag) {
        super(typeId, count, tag);
        internal = createItem();
    }

    @Override
    public void update() {

        AdapterManager.getAdapter().setTag(internal, tag);
        internal.setAmount(count);
    }

    @Override
    protected MComponent getTranslationComponent() {

        NamespacedKey key = internal.getType().getKey();
        return new MTranslateComponent((internal.getType().isBlock() ? "block" : "item") + "." + key.getNamespace() + "." + key.getKey());
    }

    private ItemStack createItem() {

        Material mat = Material.matchMaterial(getType().toString());

        if(mat == null) {
            MidnightCoreAPI.getLogger().warn("Unable to find material for " + getType().getPath().toUpperCase(Locale.ENGLISH));
            return null;
        }

        ItemStack is = new ItemStack(mat, count);
        AdapterManager.getAdapter().setTag(is, tag);

        return is;
    }

    public ItemStack getInternal() {
        return internal;
    }
}
