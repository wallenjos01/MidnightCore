package org.wallentines.mcore.savepoint;

import net.minecraft.server.level.ServerPlayer;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mdcfg.ConfigSection;

import java.util.EnumSet;

public class FabricSavepointModule extends SavepointModule {


    @Override
    protected Savepoint createSavepoint(Player player, EnumSet<SaveFlag> flags) {

        if(!(player instanceof ServerPlayer spl)) {
            throw new IllegalArgumentException("Attempt to create a savepoint for a non-ServerPlayer!");
        }

        return FabricSavepoint.create(spl, flags);
    }

    @Override
    public boolean initialize(ConfigSection section, Server data) {
        return true;
    }
}
