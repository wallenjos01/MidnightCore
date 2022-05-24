package org.wallentines.midnightcore.common.item;

import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

public interface ItemConverter {

    MItemStack create(Identifier id, int count, ConfigSection nbt);

}
