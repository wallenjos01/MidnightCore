package org.wallentines.midnightcore.fabric.module.savepoint;

import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.common.module.savepoint.AbstractSavepoint;
import org.wallentines.midnightcore.common.module.savepoint.AbstractSavepointModule;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

public class FabricSavepointModule extends AbstractSavepointModule {

    @Override
    public void resetPlayer(MPlayer player) {

        ServerPlayer pl = FabricPlayer.getInternal(player);
        if(pl == null) return;

        pl.getInventory().clearContent();
        pl.removeAllEffects();
        pl.setRemainingFireTicks(0);
        pl.setHealth(pl.getMaxHealth());
        pl.getFoodData().setFoodLevel(20);
        pl.getFoodData().eat(1, 20);
        pl.getFoodData().tick(pl);

    }

    @Override
    public AbstractSavepoint createSavepoint(Identifier id) {

        return new FabricSavepoint(id);
    }

    public static final ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>> MODULE_INFO = new ModuleInfo<>(FabricSavepointModule::new, ID, DEFAULT_CONFIG);

}
