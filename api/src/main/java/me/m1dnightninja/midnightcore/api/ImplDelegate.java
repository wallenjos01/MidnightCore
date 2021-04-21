package me.m1dnightninja.midnightcore.api;

import me.m1dnightninja.midnightcore.api.text.AbstractActionBar;
import me.m1dnightninja.midnightcore.api.text.AbstractCustomScoreboard;
import me.m1dnightninja.midnightcore.api.text.AbstractTitle;
import me.m1dnightninja.midnightcore.api.text.MComponent;

import java.util.UUID;

public interface ImplDelegate {
    AbstractTimer createTimer(MComponent prefix, int time, boolean countUp, AbstractTimer.TimerCallback callback);

    AbstractInventoryGUI createInventoryGUI(MComponent title);

    AbstractTitle createTitle(MComponent comp, AbstractTitle.TitleOptions opts);

    AbstractActionBar createActionBar(MComponent comp, AbstractActionBar.ActionBarOptions opts);

    AbstractCustomScoreboard createCustomScoreboard(String id, MComponent title);

    boolean hasPermission(UUID u, String permission);
}

