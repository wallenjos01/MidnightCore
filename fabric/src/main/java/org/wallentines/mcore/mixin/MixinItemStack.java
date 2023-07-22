package org.wallentines.mcore.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.wallentines.mcore.item.ItemStack;
import org.wallentines.mcore.util.NBTContext;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightlib.registry.Identifier;

@Mixin(net.minecraft.world.item.ItemStack.class)
public abstract class MixinItemStack implements ItemStack {

    @Shadow public abstract Item getItem();

    @Shadow public abstract int getCount();
    @Shadow public abstract void setCount(int count);
    @Shadow public abstract void shrink(int count);
    @Shadow public abstract void grow(int count);

    @Shadow @Nullable private CompoundTag tag;

    @Shadow public abstract void setTag(@Nullable CompoundTag compoundTag);

    @Unique
    @Override
    public Identifier getType() {
        return ConversionUtil.toIdentifier(BuiltInRegistries.ITEM.getKey(getItem()));
    }

    @Unique
    @Override
    public ConfigSection getTag() {
        return tag == null ? null : NBTContext.INSTANCE.convert(ConfigContext.INSTANCE, tag).asSection();
    }

    @Unique
    @Override
    public void setTag(ConfigSection nbt) {
        setTag(nbt == null ? null : (CompoundTag) ConfigContext.INSTANCE.convert(NBTContext.INSTANCE, nbt));
    }

}
