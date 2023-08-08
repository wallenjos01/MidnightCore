package org.wallentines.mcore;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import org.wallentines.mcore.item.ItemStack;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mcore.util.RegistryUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

public class ItemFactory implements ItemStack.Factory {
    @Override
    public ItemStack build(Identifier type, int count, ConfigSection tag, byte legacyData) {
        if(legacyData != -1) {
            throw new IllegalStateException("ItemStack data value requested for an unsupported version!");
        }

        Item it = ((DefaultedRegistry<Item>) RegistryUtil.registryOrThrow(Registries.ITEM)).get(ConversionUtil.toResourceLocation(type));

        ItemStack out = new net.minecraft.world.item.ItemStack(it, count);
        out.setTag(tag);
        return out;
    }
}
