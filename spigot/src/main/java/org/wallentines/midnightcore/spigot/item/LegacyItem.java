package org.wallentines.midnightcore.spigot.item;

import org.bukkit.inventory.ItemStack;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MTranslateComponent;
import org.wallentines.midnightcore.common.item.AbstractItem;
import org.wallentines.midnightcore.spigot.adapter.AdapterManager;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Locale;

@SuppressWarnings("deprecation")
public class LegacyItem extends AbstractItem {

    private final ItemStack internal;
    private short data;

    public LegacyItem(ItemStack is) {
        super(new Identifier("minecraft", is.getType().name()), is.getAmount(), AdapterManager.getAdapter().getTag(is));
        internal = AdapterManager.getAdapter().setupInternal(is);

    }
    public LegacyItem(Identifier typeId, int count, ConfigSection tag) {
        super(typeId, count, tag);

        internal = createItem();
    }

    @Override
    public void update() {

        internal.setAmount(count);
        AdapterManager.getAdapter().setTag(internal, tag);
    }

    @Override
    protected MComponent getTranslationComponent() {

        String name = internal.getType().name().toLowerCase(Locale.ROOT);
        StringBuilder camelCase = new StringBuilder();
        for(int i = 0 ; i < name.length() ; i++) {
            char c = name.charAt(i);
            if(c == '_' && i + 1 < name.length()) {
                i++;
                c = name.charAt(i);
                if(c >= 'a' && c <= 'z') {
                    camelCase.append(c - 32);
                    continue;
                }
            }
            camelCase.append(c);
        }

        return new MTranslateComponent((internal.getType().isBlock() ? "tile" : "item") + "." + camelCase + ".name");
    }

    private ItemStack createItem() {

        ItemStack out = AdapterManager.getAdapter().setupInternal(LegacyUtil.fromLegacyMaterial(getType(), data));
        if(out == null) {
            MidnightCoreAPI.getLogger().warn("Unable to find material for " + getType().getPath().toUpperCase(Locale.ROOT) + "!");
            return null;
        }

        if(out.getData() != null) data = out.getDurability();
        out.setAmount(count);

        AdapterManager.getAdapter().setTag(out, getTag());
        out.setItemMeta(out.getItemMeta());

        return out;
    }

    public ItemStack getInternal() {
        return internal;
    }
}
