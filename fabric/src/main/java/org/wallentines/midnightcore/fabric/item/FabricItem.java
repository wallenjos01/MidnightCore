package org.wallentines.midnightcore.fabric.item;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.wallentines.midnightcore.api.text.*;
import org.wallentines.midnightcore.common.item.AbstractItem;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

public class FabricItem extends AbstractItem {

    ItemStack internal;

    public FabricItem(ItemStack existing) {
        super(ConversionUtil.toIdentifier(Registry.ITEM.getKey(existing.getItem())), existing.getCount(), ConversionUtil.toConfigSection(existing.getTag()));
        internal = existing;
    }

    public FabricItem(Identifier typeId, int count, ConfigSection tag) {
        super(typeId, count, tag);

        if(tag == null) tag = new ConfigSection();

        internal = new ItemStack(Registry.ITEM.get(ConversionUtil.toResourceLocation(typeId)), count);
        internal.setTag(ConversionUtil.toCompoundTag(tag));

    }

    @Override
    protected MComponent getTranslationComponent() {
        MTranslateComponent out = new MTranslateComponent(internal.getDescriptionId());

        int id = internal.getRarity().color.getId();
        if(id > -1) out.getStyle().withColor(TextColor.fromRGBI(id));
        return out;
    }

    @Override
    public void update() {

        CompoundTag tag = ConversionUtil.toCompoundTag(this.tag);

        internal.setCount(this.count);
        internal.setTag(tag);
    }

    public ItemStack getInternal() {
        return internal;
    }
}
