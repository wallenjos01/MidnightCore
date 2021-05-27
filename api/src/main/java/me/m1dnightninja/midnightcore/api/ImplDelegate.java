package me.m1dnightninja.midnightcore.api;

import me.m1dnightninja.midnightcore.api.inventory.AbstractInventoryGUI;
import me.m1dnightninja.midnightcore.api.text.*;


public interface ImplDelegate {

    AbstractTimer createTimer(MComponent prefix, int time, boolean countUp, AbstractTimer.TimerCallback callback);

    AbstractInventoryGUI createInventoryGUI(MComponent title);

    AbstractCustomScoreboard createCustomScoreboard(String id, MComponent title);

}

