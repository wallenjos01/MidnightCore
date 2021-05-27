package me.m1dnightninja.midnightcore.api.inventory;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;

public interface ItemConverter {

    MItemStack createItem(MIdentifier type, int count, ConfigSection tag);

}
