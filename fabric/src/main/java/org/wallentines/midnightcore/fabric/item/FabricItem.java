package org.wallentines.midnightcore.fabric.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.*;
import org.wallentines.midnightcore.common.item.AbstractItem;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.requirement.RequirementType;

import java.util.Optional;

public class FabricItem extends AbstractItem {

    ItemStack internal;

    public FabricItem(ItemStack existing) {
        super(ConversionUtil.toIdentifier(BuiltInRegistries.ITEM.getKey(existing.getItem())), existing.getCount(), ConversionUtil.toConfigSection(existing.getTag()));
        internal = existing;
    }

    public FabricItem(Identifier typeId, int count, ConfigSection tag) {
        super(typeId, count, tag);

        if(tag == null) tag = new ConfigSection();

        internal = new ItemStack(BuiltInRegistries.ITEM.get(ConversionUtil.toResourceLocation(typeId)), count);
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


    public static final RequirementType<MPlayer> ITEM_REQUIREMENT = (pl,req,item) -> {
        ServerPlayer sp = FabricPlayer.getInternal(pl);

        int index = item.indexOf(",");

        String id = item;
        int count = 1;

        if(index > -1) {
            id = item.substring(0, index);
            count = Integer.parseInt(item.substring(index + 1));
        }
        if(id.length() == 0) return false;

        ResourceLocation loc = new ResourceLocation(id);
        Optional<Item> oit = BuiltInRegistries.ITEM.getOptional(loc);
        if(oit.isEmpty()) return false;

        Item it = oit.get();
        int items = ContainerHelper.clearOrCountMatchingItems(sp.getInventory(), is -> is.getItem() == it, 0, true);
        items += ContainerHelper.clearOrCountMatchingItems(sp.containerMenu.getCarried(), is -> is.getItem() == it, 0, true);

        return items >= count;
    };

}
