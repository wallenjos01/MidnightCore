package org.wallentines.midnightcore.common.module.savepoint;

import org.wallentines.midnightcore.api.module.savepoint.Savepoint;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

public abstract class AbstractSavepoint implements Savepoint {

    private final Identifier id;
    protected ConfigSection extraData;

    protected AbstractSavepoint(Identifier id) {
        this.id = id;
        this.extraData = new ConfigSection();
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
