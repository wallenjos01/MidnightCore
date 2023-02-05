package org.wallentines.midnightcore.api.module.messaging;

import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface MessagingModule extends ServerModule {

    void registerHandler(Identifier id, MessageHandler handler);

    void sendMessage(MPlayer player, Identifier id, ConfigSection data);

    void sendRawMessage(MPlayer player, Identifier id, byte[] data);

    void addLoginListener(Consumer<LoginNegotiator> onLogin);

    void unregisterHandler(Identifier id);

}
