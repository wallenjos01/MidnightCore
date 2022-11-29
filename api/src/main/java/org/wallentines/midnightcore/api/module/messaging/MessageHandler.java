package org.wallentines.midnightcore.api.module.messaging;

import io.netty.buffer.ByteBuf;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.config.ConfigSection;

import java.io.DataInput;

public interface MessageHandler {

    void handle(MPlayer player, ByteBuf configSection);

    /**
     * If true, messages under this channel from servers will be sent to clients.
     * Use this is the client and server need to communicate directly, as opposed to
     * the server and proxy.
     * NOTE: If there is no proxy between the client and server, this value will effectively
     * always be true!
     *
     * @return Whether this packet should be visible to players.
     */
    default boolean visibleToPlayers() { return false; }

}
