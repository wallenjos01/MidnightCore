package org.wallentines.mcore;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.wallentines.mcore.util.ItemComponentUtil;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mcore.util.RegistryUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Optional;

public class ItemFactory implements ItemStack.Factory {

    @Override
    public ItemStack buildLegacy(Identifier id, int count, byte damage, ConfigSection tag, GameVersion version) {
        throw new IllegalStateException("Unable to build legacy ItemStack for this version!");
    }

    @Override
    public ItemStack buildTagged(Identifier id, int count, ConfigSection tag, GameVersion version) {
        throw new IllegalStateException("Unable to build tagged ItemStack for this version!");
    }

    @Override
    public ItemStack buildStructured(Identifier id, int count, ItemStack.ComponentPatchSet components, GameVersion version) {

        Optional<Holder.Reference<Item>> it = RegistryUtil.registryOrThrow(Registries.ITEM).get(ConversionUtil.toResourceLocation(id));
        if(it.isEmpty()) {
            throw new RuntimeException("Unable to find item with ID " + id);
        }

        net.minecraft.world.item.ItemStack out = new net.minecraft.world.item.ItemStack(it.get().value(), count);

        if(components != null) {
            DataComponentPatch.Builder patch = DataComponentPatch.builder();
            for (Identifier cmp : components.components.keySet()) {

                ResourceLocation loc = ConversionUtil.toResourceLocation(cmp);
                Optional<Holder.Reference<DataComponentType<?>>> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(loc);
                if(type.isEmpty()) {
                    continue;
                }

                patch.set(ItemComponentUtil.decodeTyped(type.get().value(), components.get(cmp)));
            }
            for (Identifier cmp : components.removedComponents) {

                ResourceLocation loc = ConversionUtil.toResourceLocation(cmp);
                Optional<Holder.Reference<DataComponentType<?>>> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(loc);
                if(type.isEmpty()) {
                    continue;
                }
                patch.remove(type.get().value());
            }
            out.applyComponents(patch.build());
        }

        return out;
    }

}
