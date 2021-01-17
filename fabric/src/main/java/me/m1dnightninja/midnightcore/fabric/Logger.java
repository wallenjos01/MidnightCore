package me.m1dnightninja.midnightcore.fabric;

import me.m1dnightninja.midnightcore.api.ILogger;

public class Logger implements ILogger {

    private final org.apache.logging.log4j.Logger logger;

    public Logger(org.apache.logging.log4j.Logger log) {
        logger = log;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }
}
