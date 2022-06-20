package org.wallentines.midnightcore.fabric.module.dynamiclevel;

import net.minecraft.world.level.block.Blocks;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

public class DynamicLevelModule implements Module<MidnightCoreAPI> {

    private final Registry<PortalType> portalTypeRegistry = new Registry<>();

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {

        portalTypeRegistry.register(new Identifier("minecraft", "nether"), new PortalType(Blocks.NETHER_PORTAL));
        portalTypeRegistry.register(new Identifier("minecraft", "the_end"), new PortalType(Blocks.END_PORTAL));

        return true;
    }

    @Override
    public void reload(ConfigSection config) {

    }

    @Override
    public void disable() {

    }
}
