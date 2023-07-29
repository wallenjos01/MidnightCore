package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A functional interface for handling custom packets received from clients during the login phase
 */
public interface ServerLoginPacketHandler {

    void handle(UUID playerId, String playerName, @Nullable ByteBuf responseData);

}
