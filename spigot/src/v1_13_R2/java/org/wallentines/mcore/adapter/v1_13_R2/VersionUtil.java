package org.wallentines.mcore.adapter.v1_13_R2;

import net.minecraft.server.v1_13_R2.ServerPing;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.wallentines.mcore.GameVersion;

public class VersionUtil {

    public static GameVersion getGameVersion() {

        ServerPing.ServerData data = ((CraftServer) Bukkit.getServer()).getServer().getServerPing().getServerData();
        return new GameVersion(data.a(), data.getProtocolVersion());
    }

}
