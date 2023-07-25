package org.wallentines.mcore.savepoint;

import org.wallentines.mcore.GameMode;
import org.wallentines.mcore.Location;
import org.wallentines.mcore.Player;
import org.wallentines.mdcfg.ConfigSection;

public abstract class Savepoint {

    protected final ConfigSection extraData = new ConfigSection();
    protected final Location location;
    protected final GameMode gameMode;
    protected final ConfigSection nbt;

    protected Savepoint(Location location, GameMode gameMode, ConfigSection nbt) {
        this.location = location;
        this.gameMode = gameMode;
        this.nbt = nbt;
    }

    public Location getLocation() {
        return location;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public ConfigSection getNBT() {
        return nbt;
    }

    public ConfigSection getExtraData() {
        return extraData;
    }


    public abstract void load(Player player);

}
