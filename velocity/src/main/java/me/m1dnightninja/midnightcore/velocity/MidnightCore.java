package me.m1dnightninja.midnightcore.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.m1dnightninja.midnightcore.api.inventory.AbstractInventoryGUI;
import me.m1dnightninja.midnightcore.api.text.AbstractTimer;
import me.m1dnightninja.midnightcore.api.ImplDelegate;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.text.ActionBar;
import me.m1dnightninja.midnightcore.api.text.AbstractCustomScoreboard;
import me.m1dnightninja.midnightcore.api.text.Title;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.common.JavaLogger;
import me.m1dnightninja.midnightcore.common.config.JsonConfigProvider;
import me.m1dnightninja.midnightcore.velocity.listener.CustomPayloadListener;
import me.m1dnightninja.midnightcore.velocity.module.LastJoinedModule;
import me.m1dnightninja.midnightcore.velocity.module.PlayerDataModule;
import me.m1dnightninja.midnightcore.velocity.text.Timer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

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

        getServer().getEventManager().register(this, new CustomPayloadListener());



        new MidnightCoreAPI(new JavaLogger(logger), new ImplDelegate() {
            @Override
            public AbstractTimer createTimer(MComponent name, int seconds, boolean countUp, AbstractTimer.TimerCallback callback) {
                return new Timer(name, seconds, countUp, callback);
            }

            @Override
            public AbstractInventoryGUI createInventoryGUI(MComponent name) {
                throw new IllegalStateException("Cannot create Inventory GUI on proxy!");
            }

            @Override
            public AbstractCustomScoreboard createCustomScoreboard(String id, MComponent title) {
                return null;
            }

        }, null, null, new JsonConfigProvider(), dataFolder.toFile(), new PlayerDataModule(), new LastJoinedModule());
    }

    public ProxyServer getServer() {
        return server;
    }

    public static MidnightCore getInstance() {
        return instance;
    }

}
