package me.m1dnightninja.midnightcore.api.module;

import java.util.UUID;
import me.m1dnightninja.midnightcore.api.IModule;

public interface ISavePointModule
extends IModule {
    void savePlayer(UUID var1, String var2);

    void loadPlayer(UUID var1, String var2);

    void removeSavePoint(UUID var1, String var2);

    void resetPlayer(UUID var1);
}

