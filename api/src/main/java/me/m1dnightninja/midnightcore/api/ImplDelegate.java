package me.m1dnightninja.midnightcore.api;

import me.m1dnightninja.midnightcore.api.text.MComponent;

import java.util.UUID;

public interface ImplDelegate {
    AbstractTimer createTimer(MComponent prefix, int time, boolean countUp, AbstractTimer.TimerCallback callback);

    AbstractInventoryGUI createInventoryGUI(MComponent title);

    boolean hasPermission(UUID u, String permission);
}

