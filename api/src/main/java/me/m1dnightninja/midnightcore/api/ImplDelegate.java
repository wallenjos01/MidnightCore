package me.m1dnightninja.midnightcore.api;

public interface ImplDelegate {
    AbstractTimer createTimer(String var1, int var2, boolean var3, AbstractTimer.TimerCallback var4);

    AbstractInventoryGUI<?> createInventoryGUI(String var1);
}

