package me.m1dnightninja.midnightcore.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.m1dnightninja.midnightcore.api.AbstractInventoryGUI;
import me.m1dnightninja.midnightcore.api.AbstractTimer;
import me.m1dnightninja.midnightcore.api.ImplDelegate;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.text.AbstractActionBar;
import me.m1dnightninja.midnightcore.api.text.AbstractCustomScoreboard;
import me.m1dnightninja.midnightcore.api.text.AbstractTitle;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.common.JavaLogger;
import me.m1dnightninja.midnightcore.common.JsonConfigProvider;
import me.m1dnightninja.midnightcore.velocity.module.LastJoinedModule;
import me.m1dnightninja.midnightcore.velocity.module.PlayerDataModule;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Plugin(id = "midnightcore", name = "MidnightCore", version = "1.0.0", authors = { "M1dnight_Ninja" })
public class MidnightCore {

    private static MidnightCore instance;
    private final ProxyServer server;

    private final Logger logger;
    private final Path dataFolder;

    @Inject
    public MidnightCore(ProxyServer server, Logger logger, @DataDirectory Path dataFolder) {

        instance = this;

        this.server = server;
        this.logger = logger;
        this.dataFolder = dataFolder;

    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {

        new MidnightCoreAPI(new JavaLogger(logger), new ImplDelegate() {
            @Override
            public AbstractTimer createTimer(MComponent name, int seconds, boolean countUp, AbstractTimer.TimerCallback callback) {
                return new Timer(name, seconds, countUp, callback);
            }

            @Override
            public AbstractInventoryGUI createInventoryGUI(MComponent name) {
                throw new IllegalStateException("Cannot create Inventory GUI on proxy!");
            }

            // TODO: Implement titles and scoreboards on Proxy
            @Override
            public AbstractTitle createTitle(MComponent comp, AbstractTitle.TitleOptions opts) {
                return null;
            }

            @Override
            public AbstractActionBar createActionBar(MComponent comp, AbstractActionBar.ActionBarOptions opts) {
                return null;
            }

            @Override
            public AbstractCustomScoreboard createCustomScoreboard(String id, MComponent title) {
                return null;
            }

            @Override
            public boolean hasPermission(UUID u, String permission) {

                Optional<Player> p = server.getPlayer(u);
                return p.map(player -> player.hasPermission(permission)).orElse(false);

            }
        }, new JsonConfigProvider(), dataFolder.toFile(), new PlayerDataModule(), new LastJoinedModule());
    }

    public ProxyServer getServer() {
        return server;
    }

    public static MidnightCore getInstance() {
        return instance;
    }

}
