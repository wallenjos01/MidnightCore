package org.wallentines.mcore.savepoint;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.mcore.GameMode;
import org.wallentines.mcore.Location;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.util.NBTContext;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;

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
            spl.load((CompoundTag) ConfigContext.INSTANCE.convert(NBTContext.INSTANCE, nbt));
        }
        if(gameMode != null) {
            player.setGameMode(gameMode);
        }
        if(location != null) {
            player.teleport(location);
        }
        if(advancementData != null) {
            ((AdvancementExtension) spl.getAdvancements()).loadFromMap(advancementData.advancements(), spl.server.getAdvancements());
        }
    }


    public static FabricSavepoint create(ServerPlayer player, EnumSet<SavepointModule.SaveFlag> flags) {

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
            player.saveWithoutId(out);
            nbt = NBTContext.INSTANCE.convert(ConfigContext.INSTANCE, out).asSection();
        }
        if (flags.contains(SavepointModule.SaveFlag.ADVANCEMENTS)) {

            data = new AdvancementData(
                    ((AdvancementExtension) player.getAdvancements()).saveToMap(),
                    SharedConstants.getCurrentVersion().getDataVersion().getVersion()
            );
        }

        return new FabricSavepoint(loc, mode, nbt, data);

    }

}
