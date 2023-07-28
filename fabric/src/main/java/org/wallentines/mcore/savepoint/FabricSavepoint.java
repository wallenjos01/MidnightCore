package org.wallentines.mcore.savepoint;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import org.wallentines.mcore.*;
import org.wallentines.mcore.util.NBTContext;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.module.ModuleInfo;

import java.util.EnumSet;

public class FabricSavepoint extends Savepoint {

    private final AdvancementData advancementData;

    public FabricSavepoint(Location location, GameMode gameMode, ConfigSection nbt, AdvancementData data) {
        super(location, gameMode, nbt);
        this.advancementData = data;
    }

    @Override
    public void load(Player player) {

        if(!(player instanceof ServerPlayer spl)) {
            throw new IllegalArgumentException("Attempt to load savepoint for non-ServerPlayer!");
        }

        if(nbt != null) {

            spl.removeAllEffects();
            spl.load((CompoundTag) ConfigContext.INSTANCE.convert(NBTContext.INSTANCE, nbt));
            for(MobEffectInstance inst : spl.getActiveEffects()) {
                spl.connection.send(new ClientboundUpdateMobEffectPacket(spl.getId(), inst));
            }
        }
        if(gameMode != null) {
            player.setGameMode(gameMode);
        }
        if(location != null) {
            player.teleport(location);
        }
        if(advancementData != null) {
            advancementData.load(spl);
        }
    }


    public static FabricSavepoint create(Player player, EnumSet<SavepointModule.SaveFlag> flags) {

        if(!(player instanceof ServerPlayer spl)) {
            throw new IllegalArgumentException("Attempt to load savepoint for non-ServerPlayer!");
        }

        Location loc = null;
        GameMode mode = null;
        ConfigSection nbt = null;
        AdvancementData data = null;

        if(flags.contains(SavepointModule.SaveFlag.LOCATION)) {
            loc = player.getLocation();
        }
        if(flags.contains(SavepointModule.SaveFlag.GAME_MODE)) {

            mode = player.getGameMode();
        }
        if(flags.contains(SavepointModule.SaveFlag.NBT)) {

            CompoundTag out = new CompoundTag();
            spl.saveWithoutId(out);
            nbt = NBTContext.INSTANCE.convert(ConfigContext.INSTANCE, out).asSection();
        }
        if (flags.contains(SavepointModule.SaveFlag.ADVANCEMENTS)) {

            data = AdvancementData.save(((ServerPlayer) player).getAdvancements());
        }

        return new FabricSavepoint(loc, mode, nbt, data);
    }


    public static final Serializer<FabricSavepoint> SERIALIZER = ObjectSerializer.create(
            Location.SERIALIZER.<FabricSavepoint>entry("location", Savepoint::getLocation).optional(),
            GameMode.SERIALIZER.<FabricSavepoint>entry("game_mode", Savepoint::getGameMode).optional(),
            ConfigSection.SERIALIZER.<FabricSavepoint>entry("nbt", Savepoint::getNBT).optional(),
            AdvancementData.SERIALIZER.<FabricSavepoint>entry("advancements", fsp -> fsp.advancementData).optional(),
            ConfigSection.SERIALIZER.<FabricSavepoint>entry("extra", Savepoint::getExtraData).optional(),
            (loc, mode, nbt, adv, extra) -> {
                FabricSavepoint out = new FabricSavepoint(loc, mode, nbt, adv);
                out.getExtraData().fillOverwrite(extra);
                return out;
            }
    );

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<>(FabricSavepointModule::new, SavepointModule.ID, new ConfigSection());

}
