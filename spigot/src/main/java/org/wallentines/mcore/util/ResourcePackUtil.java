package org.wallentines.mcore.util;

import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.ResourcePack;
import org.wallentines.mcore.SpigotPlayer;
import org.wallentines.mcore.lang.PlaceholderContext;
import org.wallentines.mcore.lang.UnresolvedComponent;

import java.util.UUID;

public class ResourcePackUtil {

    public static void addResourcePack(SpigotPlayer player, ResourcePack pack) {

        PlaceholderContext ctx = new PlaceholderContext();
        ctx.addValue(player);

        UnresolvedComponent msg = pack.message();
        String message = msg == null ? null : msg.resolve(ctx).toLegacyText();

        GameVersion version = player.getServer().getVersion();
        if(version.hasFeature(GameVersion.Feature.RESOURCE_PACK_IDS)) {
            player.getInternal().addResourcePack(pack.uuid(), pack.url(), pack.hash().getBytes(), message, pack.forced());

            // Although pack message support was added in 1.17, the Bukkit API didn't support it until 1.18 (PV757)
        } else if(version.getProtocolVersion() >= 757) {
            player.getInternal().setResourcePack(pack.url(), pack.hash().getBytes(), message, pack.forced());
        } else {
            player.getInternal().setResourcePack(pack.url(), pack.hash().getBytes());
        }
    }

    public static void removeResourcePack(SpigotPlayer player, UUID uuid) {
        if(player.getServer().getVersion().hasFeature(GameVersion.Feature.RESOURCE_PACK_IDS)) {
            player.getInternal().removeResourcePack(uuid);
        }
    }

    public static void clearResourcePacks(SpigotPlayer player) {
        if(player.getServer().getVersion().hasFeature(GameVersion.Feature.RESOURCE_PACK_IDS)) {
            player.getInternal().removeResourcePacks();
        }
    }

}
