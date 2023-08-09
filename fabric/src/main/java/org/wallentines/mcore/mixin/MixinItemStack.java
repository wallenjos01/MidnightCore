package org.wallentines.mcore.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.wallentines.mcore.item.ItemStack;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.WrappedComponent;
import org.wallentines.mcore.util.ItemUtil;
import org.wallentines.mcore.util.NBTContext;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mcore.util.RegistryUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Optional;

@Mixin(net.minecraft.world.item.ItemStack.class)
@Implements(@Interface(iface= ItemStack.class, prefix = "mcore$"))
public abstract class MixinItemStack implements ItemStack {

    @Shadow public abstract Item getItem();

    @Shadow public abstract int getCount();
    @Shadow public abstract void setCount(int count);
    @Shadow public abstract void shrink(int count);
    @Shadow public abstract void grow(int count);

    @Shadow @Nullable private CompoundTag tag;

    @Shadow public abstract void setTag(@Nullable CompoundTag compoundTag);

    @Shadow public abstract net.minecraft.world.item.ItemStack setHoverName(net.minecraft.network.chat.@Nullable Component component);

    public Identifier mcore$getType() {
        return RegistryUtil.registry(Registries.ITEM)
                .flatMap(res -> Optional.ofNullable(res.getKey(getItem())))
                .map(ConversionUtil::toIdentifier)
                .orElse(new Identifier("minecraft", "air"));
    }

    public ConfigSection mcore$getTag() {
        return tag == null ? null : NBTContext.INSTANCE.convert(ConfigContext.INSTANCE, tag).asSection();
    }

    public void mcore$setTag(ConfigSection nbt) {
        setTag(nbt == null ? null : (CompoundTag) ConfigContext.INSTANCE.convert(NBTContext.INSTANCE, nbt));
    }

    public void mcore$setName(Component component) {
        setHoverName(new WrappedComponent(ItemUtil.applyItemNameBaseStyle(component)));
    }
}
