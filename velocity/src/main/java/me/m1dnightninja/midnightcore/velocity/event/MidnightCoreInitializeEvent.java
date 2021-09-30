package me.m1dnightninja.midnightcore.velocity.event;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.velocity.MidnightCore;

public class MidnightCoreInitializeEvent {

    private final MidnightCore instance;
    private final MidnightCoreAPI api;

    public MidnightCoreInitializeEvent(MidnightCore instance, MidnightCoreAPI api) {
        this.instance = instance;
        this.api = api;
    }

    public MidnightCore getInstance() {
        return instance;
    }

    public MidnightCoreAPI getApi() {
        return api;
    }
}
