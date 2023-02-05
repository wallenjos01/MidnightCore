package org.wallentines.midnightcore.fabric.module.savepoint;

import net.minecraft.server.level.ServerPlayer;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.module.savepoint.Savepoint;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.common.module.savepoint.AbstractSavepoint;
import org.wallentines.midnightcore.common.module.savepoint.AbstractSavepointModule;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
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

    @Override
    public Serializer<Savepoint> getSerializer() {
        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, Savepoint value) {
                if(!(value instanceof FabricSavepoint)) return SerializeResult.failure(value + " is not a Fabric savepoint!");
                return FabricSavepoint.SERIALIZER.serialize(context, (FabricSavepoint) value);
            }

            @Override
            public <O> SerializeResult<Savepoint> deserialize(SerializeContext<O> context, O value) {
                return FabricSavepoint.SERIALIZER.deserialize(context, value).flatMap(sp -> sp);
            }
        };
    }

    public static final ModuleInfo<MServer, ServerModule> MODULE_INFO = new ModuleInfo<>(FabricSavepointModule::new, ID, DEFAULT_CONFIG);

}
