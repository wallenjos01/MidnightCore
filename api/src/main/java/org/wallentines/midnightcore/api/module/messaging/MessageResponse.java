package org.wallentines.midnightcore.api.module.messaging;

import org.wallentines.midnightlib.config.ConfigSection;


public interface MessageResponse {

    byte[] getRawData();

    ConfigSection parseConfigSection();
}
