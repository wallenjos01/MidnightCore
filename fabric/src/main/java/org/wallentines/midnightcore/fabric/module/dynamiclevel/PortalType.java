package org.wallentines.midnightcore.fabric.module.dynamiclevel;

import net.minecraft.world.level.block.Block;

public class PortalType {

    private final Block portalBlock;

    public PortalType(Block portalBlock) {
        this.portalBlock = portalBlock;
    }

    public Block getPortalBlock() {
        return portalBlock;
    }
}
