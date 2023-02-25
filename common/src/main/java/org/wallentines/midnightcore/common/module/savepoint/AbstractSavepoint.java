package org.wallentines.midnightcore.common.module.savepoint;

import org.wallentines.midnightcore.api.module.savepoint.Savepoint;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.EnumSet;

public abstract class AbstractSavepoint implements Savepoint {

    private final Identifier id;
    protected final EnumSet<SaveFlag> flags;
    protected ConfigSection extraData;

    protected AbstractSavepoint(Identifier id, EnumSet<SaveFlag> flags) {
        this.id = id;
        this.flags = flags;
        this.extraData = new ConfigSection();
    }

    public EnumSet<SaveFlag> getFlags() {
        return flags;
    }

    public Identifier getId() {
        return id;
    }

    @Override
    public abstract boolean save(MPlayer player);

    public ConfigSection getExtraData() {
        return extraData;
    }

}
