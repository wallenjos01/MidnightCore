package org.wallentines.mcore.util;

import org.wallentines.mcore.ResourcePack;
import org.wallentines.mcore.SpigotPlayer;

import java.util.UUID;

public class ResourcePackUtil {

    public static void addResourcePack(SpigotPlayer player, ResourcePack pack) {
        player.getInternal().setResourcePack(pack.url(), pack.hash().getBytes());
    }

    public static void removeResourcePack(SpigotPlayer player, UUID uuid) {
    }

    public static void clearResourcePacks(SpigotPlayer player) {
    }

}
