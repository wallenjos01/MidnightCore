package org.wallentines.midnightcore.fabric.level;

import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.Tag;

public interface InjectedStorageAccess {

    DynamicOps<Tag> getOps();

}
