package org.wallentines.mcore.savepoint;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.module.ModuleInfo;

import java.util.EnumSet;

public class FabricSavepointModule extends SavepointModule {

    @Override
    public void resetPlayer(Player player, EnumSet<SaveFlag> flags) {

        ServerPlayer spl = ConversionUtil.validate(player);

        if(flags.contains(SaveFlag.ADVANCEMENTS)) {
            ((AdvancementExtension) spl.getAdvancements()).revokeAll(spl.server.getAdvancements());
        }
        if(flags.contains(SaveFlag.NBT)) {

            Vec3 pos = spl.position();
            spl.load(new CompoundTag());
            spl.setPos(pos);

            spl.removeAllEffects();
            spl.setRemainingFireTicks(0);
            spl.setArrowCount(0);
        }
        if(flags.contains(SaveFlag.GAME_MODE)) {
            spl.gameMode.changeGameModeForPlayer(spl.server.getDefaultGameType());
        }
    }

    @Override
    public Savepoint.Factory getFactory() {
        return FabricSavepoint::create;
    }

    @Override
    protected Savepoint createSavepoint(Player player, EnumSet<SaveFlag> flags) {
        return FabricSavepoint.create(player, flags);
    }

    @Override
    public Serializer<Savepoint> getSerializer() {
        return SERIALIZER;
    }

    private static final Serializer<Savepoint> SERIALIZER = new Serializer<>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, Savepoint value) {
            if(!(value instanceof FabricSavepoint fsp)) {
                throw new IllegalArgumentException("Unable to serialize non-Fabric Savepoint!");
            }
            return FabricSavepoint.SERIALIZER.serialize(context, fsp);
        }

        @Override
        public <O> SerializeResult<Savepoint> deserialize(SerializeContext<O> context, O value) {
            return FabricSavepoint.SERIALIZER.deserialize(context, value).flatMap(sp -> sp);
        }
    };

    @Override
    public boolean initialize(ConfigSection section, Server data) {

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            clearSavepoints(handler.getPlayer());
        });

        return true;
    }

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<>(FabricSavepointModule::new, SavepointModule.ID, new ConfigSection());
}
