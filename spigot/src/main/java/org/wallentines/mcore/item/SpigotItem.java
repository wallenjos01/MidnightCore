package org.wallentines.mcore.item;

import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

public class SpigotItem implements ItemStack {

    private final org.bukkit.inventory.ItemStack internal;

    public SpigotItem(org.bukkit.inventory.ItemStack internal) {
        this.internal = Adapter.INSTANCE.get().setupInternal(internal);
    }

    @Override
    public Identifier getType() {
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public ConfigSection getTag() {
        return null;
    }

    @Override
    public byte getLegacyDataValue() {
        return 0;
    }

    @Override
    public void setCount(int count) {

    }

    @Override
    public void setTag(ConfigSection tag) {

    }

    @Override
    public void grow(int amount) {

    }

    @Override
    public void shrink(int amount) {

    }

    public org.bukkit.inventory.ItemStack getInternal() {
        return internal;
    }
}
