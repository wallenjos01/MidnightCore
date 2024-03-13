package org.wallentines.mcore;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wallentines.mcore.util.ComponentUtil;
import org.wallentines.mcore.util.ConfigOps;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mcore.util.RegistryUtil;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Map;

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

        Item it = ((DefaultedRegistry<Item>) RegistryUtil.registryOrThrow(Registries.ITEM)).get(ConversionUtil.toResourceLocation(id));

        net.minecraft.world.item.ItemStack out = new net.minecraft.world.item.ItemStack(it, count);

        if(components != null) {
            DataComponentPatch.Builder patch = DataComponentPatch.builder();
            for (Identifier cmp : components.components.keySet()) {

                ResourceLocation loc = ConversionUtil.toResourceLocation(cmp);
                DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(loc);
                patch.set(ComponentUtil.decodeTyped(type, components.get(cmp)));
            }
            for (Identifier cmp : components.removedComponents) {

                ResourceLocation loc = ConversionUtil.toResourceLocation(cmp);
                DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(loc);
                patch.remove(type);
            }
            out.applyComponents(patch.build());
        }

        return out;
    }

}
