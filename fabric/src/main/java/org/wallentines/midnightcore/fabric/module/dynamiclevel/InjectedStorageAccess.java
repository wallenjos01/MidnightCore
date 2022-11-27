package org.wallentines.midnightcore.fabric.module.dynamiclevel;

import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.Tag;

public interface InjectedStorageAccess {

    DynamicOps<Tag> getOps();

}
