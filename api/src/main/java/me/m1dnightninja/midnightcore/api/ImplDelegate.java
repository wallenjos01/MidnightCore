package me.m1dnightninja.midnightcore.api;

public interface ImplDelegate {

    AbstractTimer createTimer(String text, int seconds, boolean countUp, AbstractTimer.TimerCallback cb);

    AbstractInventoryGUI<?> createInventoryGUI(String title);

}
