package org.wallentines.mcore.event;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import org.wallentines.mcore.messaging.FabricServerLoginNegotiator;

public record LoginQueryEvent(MinecraftServer server, GameProfile profile, FabricServerLoginNegotiator negotiator) {


}
