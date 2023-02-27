package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@SuppressWarnings("unused")
@Mixin(ArgumentTypeInfos.class)
public interface AccessorArgumentTypeInfos {

    @Accessor("BY_CLASS")
    static Map<Class<?>, ArgumentTypeInfo<?, ?>> byClassMap() {
        throw new UnsupportedOperationException();
    }

}
