package me.m1dnightninja.midnightcore.fabric.api.event;

import me.m1dnightninja.midnightcore.api.IModule;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.event.Event;

import java.util.List;

public class MidnightCoreLoadModulesEvent extends Event {

    private final MidnightCore core;
    private final List<IModule> toLoad;

    public MidnightCoreLoadModulesEvent(MidnightCore core, List<IModule> toLoad) {
        this.core = core;
        this.toLoad = toLoad;
    }

    public MidnightCore getCore() {
        return core;
    }

    public List<IModule> getToLoad() {
        return toLoad;
    }
}
