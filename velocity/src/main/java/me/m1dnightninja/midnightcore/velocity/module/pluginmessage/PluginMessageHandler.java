package me.m1dnightninja.midnightcore.velocity.module.pluginmessage;

import com.google.common.io.ByteArrayDataInput;
import me.m1dnightninja.midnightcore.api.player.MPlayer;

public interface PluginMessageHandler {

    void handle(MPlayer target, ByteArrayDataInput data);

}
