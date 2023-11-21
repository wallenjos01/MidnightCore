package org.wallentines.mcore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wallentines.mdcfg.codec.FileCodecRegistry;

public final class MidnightCoreAPI {

    /**
     * A logger with the name "MidnightCore"
     */
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightCore");

    /**
     * The mod's ID, should be used in identifiers
     */
    public static final String MOD_ID = "midnightcore";

    /**
     * The global file codec registry. Contains a JSON codec, and YAML on Spigot
     */
    public static final FileCodecRegistry FILE_CODEC_REGISTRY = new FileCodecRegistry();

}
