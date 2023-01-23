package org.wallentines.midnightcore.api.module;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightlib.module.Module;

public interface ServerModule extends Module<MServer> {

    default MidnightCoreAPI getAPI() {
        return MidnightCoreAPI.getInstance();
    }

}
