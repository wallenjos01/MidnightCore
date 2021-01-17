package me.m1dnightninja.midnightcore.fabric.api.event;

import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.event.Event;

public class MidnightCoreInitEvent extends Event {

    private final MidnightCore instance;

    public MidnightCoreInitEvent(MidnightCore instance) {
        this.instance = instance;
    }

    public MidnightCore getInstance() {
        return instance;
    }
}
