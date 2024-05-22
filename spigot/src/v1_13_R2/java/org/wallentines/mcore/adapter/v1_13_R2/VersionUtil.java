package org.wallentines.mcore.adapter.v1_13_R2;

import net.minecraft.server.v1_13_R2.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.wallentines.mcore.GameVersion;

public class VersionUtil {

    public static GameVersion getGameVersion() {

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();

        String version = server.getVersion();
        if(version.equals("1.13.1")) {
            return new GameVersion(version, 401);
        } else {
            return new GameVersion(version, 404);
        }
    }

}
