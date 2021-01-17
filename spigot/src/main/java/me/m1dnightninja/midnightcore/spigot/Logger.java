package me.m1dnightninja.midnightcore.spigot;

import me.m1dnightninja.midnightcore.api.ILogger;

public class Logger implements ILogger {

    private final java.util.logging.Logger base;

    Logger(java.util.logging.Logger base) {
        this.base = base;
    }

    @Override
    public void info(String message) {
        base.info(message);
    }

    @Override
    public void warn(String message) {
        base.warning(message);
    }

    @Override
    public void error(String message) {
        base.severe(message);
    }
}
