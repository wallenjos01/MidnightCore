package me.m1dnightninja.midnightcore.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.m1dnightninja.midnightcore.api.config.ConfigRegistry;
import me.m1dnightninja.midnightcore.api.inventory.MInventoryGUI;
import me.m1dnightninja.midnightcore.api.text.MTimer;
import me.m1dnightninja.midnightcore.api.text.MScoreboard;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.common.MidnightCoreImpl;
import me.m1dnightninja.midnightcore.common.config.JsonConfigProvider;
import me.m1dnightninja.midnightcore.velocity.module.lastjoined.LastJoinedModule;
import me.m1dnightninja.midnightcore.velocity.module.playerdata.PlayerDataModule;
import me.m1dnightninja.midnightcore.velocity.module.pluginmessage.PluginMessageModule;
import me.m1dnightninja.midnightcore.velocity.player.VelocityPlayerManager;
import me.m1dnightninja.midnightcore.velocity.text.VelocityTimer;

import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin(id = "midnightcore", name = "MidnightCore", version = "1.0.0", authors = { "M1dnight_Ninja" })
public class MidnightCore {

    private static MidnightCore instance;
    private final ProxyServer server;

    private final Path dataFolder;

    @Inject
    public MidnightCore(ProxyServer server, Logger logger, @DataDirectory Path dataFolder) {

        instance = this;

        this.server = server;
        this.dataFolder = dataFolder;

    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {

        new MidnightCoreImpl(new ConfigRegistry(), new VelocityPlayerManager(), null, JsonConfigProvider.INSTANCE, dataFolder.toFile(), new PlayerDataModule(), new LastJoinedModule(), new PluginMessageModule()) {

            @Override
            public MTimer createTimer(MComponent name, int seconds, boolean countUp, MTimer.TimerCallback callback) {
                return new VelocityTimer(name, seconds, countUp, callback);
            }

            @Override
            public MInventoryGUI createInventoryGUI(MComponent name) {
                throw new IllegalStateException("Cannot create Inventory GUI on proxy!");
            }

            @Override
            public MScoreboard createScoreboard(String id, MComponent title) {
                return null;
            }

            @Override
            public String getGameVersion() {
                return getServer().getVersion().getVersion();
            }

            @Override
            public void executeConsoleCommand(String cmd) {
                getServer().getCommandManager().executeAsync(getServer().getConsoleCommandSource(), cmd);
            }
        };

    }

    public ProxyServer getServer() {
        return server;
    }

    public static MidnightCore getInstance() {
        return instance;
    }

}
