package org.wallentines.mcore;

import org.bukkit.Material;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Locale;

public class SpigotItem implements ItemStack {

    private final org.bukkit.inventory.ItemStack internal;
    private final Identifier id;

    public SpigotItem(org.bukkit.inventory.ItemStack internal) {
        this.internal = Adapter.INSTANCE.get().setupInternal(internal);
        this.id = Adapter.INSTANCE.get().getItemId(this.internal);
    }

    public SpigotItem(Identifier id, int count, ConfigSection tag, byte data) {

        internal = Adapter.INSTANCE.get().buildItem(id, count, data);
        this.id = Adapter.INSTANCE.get().getItemId(internal);

        if(tag != null) {
            Adapter.INSTANCE.get().setTag(internal, tag);
        }

    }

    @Override
    public Identifier getType() {
        return id;
    }

    @Override
    public int getCount() {
        return internal.getAmount();
    }

    @Override
    public ConfigSection getTag() {
        return Adapter.INSTANCE.get().getTag(internal);
    }

    @Override
    @SuppressWarnings("deprecation")
    public byte getLegacyDataValue() {
        return (byte) internal.getDurability();
    }

    @Override
    public void setCount(int count) {
        internal.setAmount(count);
    }

    @Override
    public void setTag(ConfigSection tag) {
        Adapter.INSTANCE.get().setTag(internal, tag);
    }

    @Override
    public void grow(int amount) {
        setCount(internal.getAmount() + amount);
    }

    @Override
    public void shrink(int amount) {
        setCount(internal.getAmount() - amount);
    }

    @Override
    public ItemStack copy() {
        return new SpigotItem(internal.clone());
    }

    public org.bukkit.inventory.ItemStack getInternal() {
        return internal;
    }
}
