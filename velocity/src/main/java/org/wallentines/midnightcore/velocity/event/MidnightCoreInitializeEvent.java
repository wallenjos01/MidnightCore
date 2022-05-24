package org.wallentines.midnightcore.velocity.event;

import org.wallentines.midnightcore.velocity.MidnightCore;
import org.wallentines.midnightlib.event.Event;

public class MidnightCoreInitializeEvent extends Event {

    private final MidnightCore core;

    public MidnightCoreInitializeEvent(MidnightCore core) {
        this.core = core;
    }

    public MidnightCore getInstance() {
        return core;
    }
}
