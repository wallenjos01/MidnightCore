package org.wallentines.mcore.item;

import org.bukkit.Material;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Locale;

public class SpigotItem implements ItemStack {

    private final org.bukkit.inventory.ItemStack internal;
    private final Identifier id;

    public SpigotItem(org.bukkit.inventory.ItemStack internal) {
        this.internal = Adapter.INSTANCE.get().setupInternal(internal);
        if(GameVersion.CURRENT_VERSION.get().hasFeature(GameVersion.Feature.NAMESPACED_IDS)) {
            this.id = new Identifier(internal.getType().getKey().getNamespace(), internal.getType().getKey().getKey());
        } else {
            this.id = new Identifier("minecraft", internal.getType().name());
        }
    }

    @SuppressWarnings("deprecation")
    public SpigotItem(Identifier id, int count, ConfigSection tag, byte data) {

        if(GameVersion.CURRENT_VERSION.get().hasFeature(GameVersion.Feature.NAMESPACED_IDS)) {

            Material mat = Material.matchMaterial(id.toString());
            if(mat == null) {
                throw new IllegalArgumentException("Unable to find material for ID " + id + "!");
            }
            this.id = id;

            internal = Adapter.INSTANCE.get().setupInternal(new org.bukkit.inventory.ItemStack(mat, count));
            if(data != -1) {
                throw new IllegalArgumentException("Attempt to construct an item with data value on an unsupported version!");
            }

        } else {

            LegacyUtil.ItemData it = LegacyUtil.fromLegacyMaterial(id);
            if(it == null) {
                throw new IllegalArgumentException("Unable to find legacy material for ID " + id + "!");
            }

            this.id = new Identifier("minecraft", it.id.toLowerCase(Locale.ENGLISH));
            internal = Adapter.INSTANCE.get().setupInternal(new org.bukkit.inventory.ItemStack(Material.valueOf(it.id), count));

            if(data == -1) data = it.data;
            if(data != -1) {
                internal.setDurability(data);
            }

        }

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

    public org.bukkit.inventory.ItemStack getInternal() {
        return internal;
    }
}
