package org.wallentines.mcore.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.wallentines.mcore.ItemStack;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.WrappedComponent;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mcore.util.ItemUtil;
import org.wallentines.mcore.util.NBTContext;
import org.wallentines.mcore.util.RegistryUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Optional;

@Mixin(net.minecraft.world.item.ItemStack.class)
@Implements(@Interface(iface= ItemStack.class, prefix = "mcore$"))
public abstract class MixinItemStack implements ItemStack {


    @Shadow public abstract Item getItem();

    @Shadow @Nullable private CompoundTag tag;

    @Shadow public abstract void setTag(@Nullable CompoundTag compoundTag);

    @Shadow public abstract net.minecraft.world.item.ItemStack setHoverName(@Nullable net.minecraft.network.chat.Component component);


    @Intrinsic(displace = true)
    public Identifier mcore$getType() {
        return RegistryUtil.registry(Registries.ITEM)
                .flatMap(res -> Optional.ofNullable(res.getKey(getItem())))
                .map(ConversionUtil::toIdentifier)
                .orElse(new Identifier("minecraft", "air"));
    }

    @Intrinsic(displace = true)
    public ConfigSection mcore$getTag() {
        return tag == null ? null : NBTContext.INSTANCE.convert(ConfigContext.INSTANCE, tag).asSection();
    }

    public void mcore$setTag(ConfigSection nbt) {
        setTag(nbt == null ? null : (CompoundTag) ConfigContext.INSTANCE.convert(NBTContext.INSTANCE, nbt));
    }

    public void mcore$setName(Component component) {
        setHoverName(new WrappedComponent(ItemUtil.applyItemNameBaseStyle(component)));
    }

    public byte mcore$getLegacyDataValue() {
        return 0;
    }

    @Intrinsic(displace = true)
    public ItemStack mcore$copy() {
        return (ItemStack) (Object) ((net.minecraft.world.item.ItemStack) (Object) this).copy();
    }

    @Intrinsic(displace = true)
    public int mcore$getCount() {
        return ((net.minecraft.world.item.ItemStack) (Object) this).getCount();
    }

    @Intrinsic(displace = true)
    public void mcore$setCount(int count) {
        ((net.minecraft.world.item.ItemStack) (Object) this).setCount(count);
    }

    @Intrinsic(displace = true)
    public void mcore$grow(int amount) {
        ((net.minecraft.world.item.ItemStack) (Object) this).grow(amount);
    }

    @Intrinsic(displace = true)
    public void mcore$shrink(int amount) {
        ((net.minecraft.world.item.ItemStack) (Object) this).shrink(amount);
    }

    public String mcore$getTranslationKey() {
        return ((net.minecraft.world.item.ItemStack) (Object) this).getItem().getDescriptionId();
    }

    public Color mcore$getRarityColor() {

        Integer color = ((net.minecraft.world.item.ItemStack) (Object) this).getRarity().color.getColor();
        return color == null ? Color.WHITE : new Color(color);
    }
}
