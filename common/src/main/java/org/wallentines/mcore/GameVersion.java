package org.wallentines.mcore;

import org.wallentines.midnightlib.types.DefaultedSingleton;
import org.wallentines.midnightlib.types.Singleton;

/**
 * A data type which stores basic information about a Minecraft version, including version ID (i.e. 1.20.1) and
 * protocol version (i.e. 763)
 */
public class GameVersion {

    private final String id;
    private final int protocolVersion;

    /**
     * The maximum protocol version for release versions. Starting at 1.16.4-pre1, snapshot versions use numbers
     * greater than this.
     */
    public static final int RELEASE_MAX_VERSION = 0x40000000;

    /**
     * Constructs a GameVersion object with the given ID and protocol version.
     * @param id The ID of the version
     * @param protocolVersion The protocol version of the game version.
     */
    public GameVersion(String id, int protocolVersion) {
        this.id = id;
        this.protocolVersion = protocolVersion;
    }

    /**
     * Returns the GameVersion's version ID
     * @return The version ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the GameVersion's protocol version
     * @return The protocol version
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Checks whether this GameVersion supports a particular {@link Feature Feature}
     * @param feature The feature to check
     * @return Whether this GameVersion supports the given Feature
     */
    public boolean hasFeature(Feature feature) {
        return feature.check(protocolVersion);
    }


    /**
     * A singleton containing the GameVersion of the currently running server or client.
     */
    public static final Singleton<GameVersion> CURRENT_VERSION = new DefaultedSingleton<>(new GameVersion("Unknown", 0));

    public static final GameVersion VERSION_1_8 = new GameVersion("1.8", 47);
    public static final GameVersion MAX = new GameVersion("Maximum", RELEASE_MAX_VERSION);

    public enum Feature {


        /**
         * Starting in 1.16 (20w17a), Text component support full RGB colors
         */
        RGB_TEXT(713),

        /**
         * Starting in 1.16 (20w17a), Hover events started using the field "contents" rather than "value"
         */
        HOVER_CONTENTS(713),

        /**
         * Starting in 1.16 (20w12a), When UUIDs are stored in NBT, they are stored as arrays of integers rather than strings
         */
        INT_ARRAY_UUIDS(707),

        /**
         * Starting in 1.13 (17w47a), Items no longer have numeric IDs (1=stone, 2=grass, etc.) and use namespaced IDs
         * (minecraft:stone, minecraft:grass) exclusively
         */
        NAMESPACED_IDS(346),

        /**
         * Starting in 1.13 (?), Items now use components for their custom names rather than legacy text
         */
        ITEM_NAME_COMPONENTS(346),

        /**
         * In 1.12 (17w16a), The keybind component type was added
         */
        KEY_BIND_COMPONENT(322),

        /**
         * In 1.12, The show_achievement action in Hover events was removed
         */
        HOVER_SHOW_ACHIEVEMENT(0, 316),

        /**
         * In 1.9, the offhand slot was added
         */
        OFF_HAND(49),

        /**
         * In 1.20.2, A new network protocol state called "Configuration" was added.
         */
        CONFIGURATION_PROTOCOL(764, -1, 144);


        public final int minVersion;
        public final int maxVersion;
        public final int minSnapshotVersion;
        public final int maxSnapshotVersion;

        Feature(int minVersion) {
            this(minVersion, -1, 1, -1);
        }

        Feature(int minVersion, int maxVersion) {
            this(minVersion, maxVersion, maxVersion >= 0 ? -1 : 1, -1);
        }

        Feature(int minVersion, int maxVersion, int minSnapshotVersion) {
            this(minVersion, maxVersion, minSnapshotVersion, -1);
        }

        Feature(int minVersion, int maxVersion, int minSnapshotVersion, int maxSnapshotVersion) {
            this.minVersion = minVersion;
            this.maxVersion = maxVersion >= 0 ? maxVersion : RELEASE_MAX_VERSION;
            this.minSnapshotVersion = minSnapshotVersion >= 0 ? RELEASE_MAX_VERSION + minSnapshotVersion : Integer.MAX_VALUE;
            this.maxSnapshotVersion = maxSnapshotVersion >= 0 ? RELEASE_MAX_VERSION + maxSnapshotVersion : Integer.MAX_VALUE;
        }

        /**
         * Checks whether the given protocol version supports the given feature
         * @param protocolVersion The protocol version to check
         * @return Whether that protocol version supports the feature
         */
        public boolean check(int protocolVersion) {

            if(protocolVersion > RELEASE_MAX_VERSION) {
                return protocolVersion >= minSnapshotVersion && protocolVersion <= maxSnapshotVersion;
            }

            return protocolVersion >= minVersion && protocolVersion <= maxVersion;
        }

    }

}
