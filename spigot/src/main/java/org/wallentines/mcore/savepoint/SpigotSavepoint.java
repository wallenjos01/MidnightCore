package org.wallentines.mcore.savepoint;

import org.wallentines.mcore.*;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;

import java.util.EnumSet;

public class SpigotSavepoint extends Savepoint {

    protected SpigotSavepoint(Location location, GameMode gameMode, ConfigSection nbt) {
        super(location, gameMode, nbt);
    }

    @Override
    public void load(Player player) {

        SpigotPlayer spl = ConversionUtil.validate(player);


    }


    public static SpigotSavepoint save(Player player, EnumSet<SavepointModule.SaveFlag> flags) {

        SpigotPlayer sp = ConversionUtil.validate(player);

        Location loc = null;
        if(flags.contains(SavepointModule.SaveFlag.LOCATION)) {
            loc = player.getLocation();
        }

        GameMode mode = null;
        if(flags.contains(SavepointModule.SaveFlag.GAME_MODE)) {
            mode = player.getGameMode();
        }

        ConfigSection nbt = null;
        if(flags.contains(SavepointModule.SaveFlag.NBT)) {
            nbt = Adapter.INSTANCE.get().getTag(sp.getInternal());
        }

        if(flags.contains(SavepointModule.SaveFlag.ADVANCEMENTS)) {
            MidnightCoreAPI.LOGGER.warn("Saving advancement data is not supported on Spigot servers!");
        }

        return new SpigotSavepoint(loc, mode, nbt);
    }



    public static final Serializer<SpigotSavepoint> SERIALIZER = ObjectSerializer.create(
            Location.SERIALIZER.<SpigotSavepoint>entry("location", Savepoint::getLocation).optional(),
            GameMode.SERIALIZER.<SpigotSavepoint>entry("game_mode", Savepoint::getGameMode).optional(),
            ConfigSection.SERIALIZER.<SpigotSavepoint>entry("nbt", Savepoint::getNBT).optional(),
            ConfigSection.SERIALIZER.<SpigotSavepoint>entry("extra", Savepoint::getExtraData).optional(),
            (loc, mode, nbt, extra) -> {
                SpigotSavepoint out = new SpigotSavepoint(loc, mode, nbt);
                out.getExtraData().fillOverwrite(extra);
                return out;
            }
    );
}
