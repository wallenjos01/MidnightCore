package org.wallentines.midnightcore.fabric.event;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.event.Event;

public class MidnightCoreAPICreatedEvent extends Event {

    private final MidnightCoreAPI api;

    public MidnightCoreAPICreatedEvent(MidnightCoreAPI api) {
        this.api = api;
    }

    public MidnightCoreAPI getAPI() {
        return api;
    }
}
