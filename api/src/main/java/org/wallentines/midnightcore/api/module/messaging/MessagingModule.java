package org.wallentines.midnightcore.api.module.messaging;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.registry.Identifier;

public interface MessagingModule extends Module<MidnightCoreAPI> {

    void registerHandler(Identifier id, MessageHandler handler);

    void sendMessage(MPlayer player, Identifier id, ConfigSection data);

    void sendRawMessage(MPlayer player, Identifier id, byte[] data);

}
