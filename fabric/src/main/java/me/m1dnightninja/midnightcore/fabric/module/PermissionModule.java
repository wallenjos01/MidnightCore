package me.m1dnightninja.midnightcore.fabric.module;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.common.module.AbstractPermissionModule;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;

import java.io.File;

public class PermissionModule extends AbstractPermissionModule {

    public PermissionModule() {
        super(MidnightCoreAPI.getInstance().getDefaultConfigProvider().loadFromFile(new File(MidnightCore.getInstance().getConfigDirectory(), "permissions.json")));
    }
}
