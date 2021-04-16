package me.m1dnightninja.midnightcore.common;

import me.m1dnightninja.midnightcore.api.ILogger;

public class JavaLogger implements ILogger {

    private final java.util.logging.Logger base;

    public JavaLogger(java.util.logging.Logger base) {
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
