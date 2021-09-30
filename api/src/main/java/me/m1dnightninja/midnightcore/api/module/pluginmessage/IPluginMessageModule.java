package me.m1dnightninja.midnightcore.api.module.pluginmessage;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.IModule;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;

public interface IPluginMessageModule extends IModule {

    void registerProvider(MIdentifier id, IPluginMessageHandler handler);

    void sendMessage(MPlayer player, MIdentifier id, ConfigSection sec);

}
