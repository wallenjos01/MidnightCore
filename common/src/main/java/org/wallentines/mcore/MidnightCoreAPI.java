package org.wallentines.mcore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wallentines.mdcfg.codec.FileCodecRegistry;
import org.wallentines.midnightlib.types.Singleton;

import java.nio.file.Path;

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

    /**
     * The global config directory. Will be "plugins/" on Spigot and Velocity, and "config/" on Fabric
     */
    public static final Singleton<Path> GLOBAL_CONFIG_DIRECTORY = new Singleton<>();

}
