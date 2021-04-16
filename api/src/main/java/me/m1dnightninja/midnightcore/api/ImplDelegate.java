package me.m1dnightninja.midnightcore.api;

import java.util.UUID;

public interface ImplDelegate {
    AbstractTimer createTimer(String var1, int var2, boolean var3, AbstractTimer.TimerCallback var4);

    AbstractInventoryGUI<?> createInventoryGUI(String var1);

    boolean hasPermission(UUID u, String permission);
}

