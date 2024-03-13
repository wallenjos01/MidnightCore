package org.wallentines.mcore.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.ItemStack;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.WrappedComponent;
import org.wallentines.mcore.util.*;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Optional;
import java.util.stream.Stream;

@Mixin(net.minecraft.world.item.ItemStack.class)
@Implements(@Interface(iface= ItemStack.class, prefix = "mcore$"))
public abstract class MixinItemStack implements ItemStack {


    @Shadow public abstract Item getItem();


    @Shadow public abstract <T> T set(DataComponentType<? super T> par1, Object par2);

    @Shadow public abstract DataComponentMap getComponents();

    @Shadow public abstract void applyComponents(DataComponentPatch par1);

    @Shadow public abstract DataComponentPatch getComponentsPatch();

    @Intrinsic(displace = true)
    public Identifier mcore$getType() {
        return RegistryUtil.registry(Registries.ITEM)
                .flatMap(res -> Optional.ofNullable(res.getKey(getItem())))
                .map(ConversionUtil::toIdentifier)
                .orElse(new Identifier("minecraft", "air"));
    }

    public void mcore$loadComponent(Identifier id, ConfigObject config) {
        DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ConversionUtil.toResourceLocation(id));
        if(type == null) {
            MidnightCoreAPI.LOGGER.warn("Component type " + id + " is not registered!");
            return;
        }
        set(type, type.codecOrThrow().decode(ConfigOps.INSTANCE, config).getOrThrow(false, MidnightCoreAPI.LOGGER::error).getFirst());
    }

    public ConfigObject mcore$saveComponent(Identifier id) {
        DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ConversionUtil.toResourceLocation(id));
        if(type == null) {
            MidnightCoreAPI.LOGGER.warn("Component type " + id + " is not registered!");
            return null;
        }

        TypedDataComponent<?> comp = getComponents().getTyped(type);
        if(comp == null) {
            return null;
        }

        return ComponentUtil.encodeTyped(comp);
    }

    public void mcore$removeComponent(Identifier id) {

        DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ConversionUtil.toResourceLocation(id));
        applyComponents(DataComponentPatch.builder().remove(type).build());
    }

    public Stream<Identifier> mcore$getComponentIds() {
        return getComponents().stream().map(typed -> {

            ResourceLocation id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(typed.type());
            if(id == null) throw new IllegalStateException("Found unregistered component " + typed);

            return ConversionUtil.toIdentifier(id);
        });
    }

    public void mcore$setName(Component component) {

        set(DataComponents.CUSTOM_NAME, new WrappedComponent(ItemUtil.applyItemNameBaseStyle(component)));
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

    public GameVersion mcore$getVersion() {
        return new GameVersion(SharedConstants.getCurrentVersion().getId(), SharedConstants.getProtocolVersion());
    }
}
