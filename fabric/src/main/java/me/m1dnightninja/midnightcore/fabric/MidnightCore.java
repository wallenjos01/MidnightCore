package me.m1dnightninja.midnightcore.fabric;

import com.mojang.brigadier.CommandDispatcher;
import me.m1dnightninja.midnightcore.api.AbstractTimer;
import me.m1dnightninja.midnightcore.api.IModule;
import me.m1dnightninja.midnightcore.api.ImplDelegate;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.common.JsonConfigProvider;
import me.m1dnightninja.midnightcore.common.JsonWrapper;
import me.m1dnightninja.midnightcore.fabric.api.InventoryGUI;
import me.m1dnightninja.midnightcore.fabric.api.Timer;
import me.m1dnightninja.midnightcore.fabric.api.event.MidnightCoreInitEvent;
import me.m1dnightninja.midnightcore.fabric.api.event.MidnightCoreLoadGeneratorsEvent;
import me.m1dnightninja.midnightcore.fabric.api.event.MidnightCoreLoadModulesEvent;
import me.m1dnightninja.midnightcore.fabric.dimension.EmptyGenerator;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.module.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class MidnightCore implements ModInitializer {

    private static MidnightCoreAPI api;
    private static MidnightCore instance;

    private static MinecraftServer server;

    private File configDirectory;
    private ConfigProvider configProvider;

    @Override
    public void onInitialize() {

        instance = this;
        Logger logger = new Logger(LogManager.getLogger());

        configDirectory = Paths.get("config/MidnightCore").toFile();
        if(!configDirectory.exists() || !configDirectory.isDirectory()) {

            if(configDirectory.exists() && !FileUtils.deleteQuietly(configDirectory)) {
                logger.warn("Unable to create config directory!");
                return;
            }

            if(!configDirectory.mkdirs()) {
                logger.warn("Unable to create config directory!");
                return;
            }
        }

        configProvider = new JsonConfigProvider();

        File f = new File(configDirectory, "config.json");
        if(!f.exists() || f.isDirectory()) {
            new JsonWrapper().save(f);
        }

        ConfigSection mainConfig = configProvider.loadFromFile(f);

        final IModule[] modules = new IModule[] {
                new SkinModule(),
                new LangModule(),
                new DimensionModule(),
                new SavePointModule(),
                new PermissionModule()
        };

        ImplDelegate delegate = new ImplDelegate() {
            @Override
            public Timer createTimer(String text, int seconds, boolean countUp, AbstractTimer.TimerCallback cb) {
                return new Timer(text, seconds, countUp, cb);
            }

            @Override
            public InventoryGUI createInventoryGUI(String title) {
                return new InventoryGUI(title);
            }
        };

        api = new MidnightCoreAPI(
                logger,
                delegate,
                configProvider,
                f,
                mainConfig,
                loadModules(mainConfig, modules)
        );

        Event.invoke(new MidnightCoreInitEvent(this));

        ServerLifecycleEvents.SERVER_STARTING.register((minecraftServer) -> {
            server = minecraftServer;

            HashMap<ResourceLocation, ChunkGenerator> generators = new HashMap<>();

            generators.put(new ResourceLocation("midnightcore", "empty"), new EmptyGenerator(new ResourceLocation("forest")));

            MidnightCoreLoadGeneratorsEvent event = new MidnightCoreLoadGeneratorsEvent(generators);
            Event.invoke(event);

            for(Map.Entry<ResourceLocation, ChunkGenerator> ent : generators.entrySet()) {
                api.getModule(DimensionModule.class).registerGeneratorType(ent.getKey(), ent.getValue());
            }
        });

    }

    public File getConfigDirectory() {
        return configDirectory;
    }

    public ConfigProvider getDefaultConfigProvider() {
        return configProvider;
    }

    private IModule[] loadModules(ConfigSection mainConfig, IModule... modules) {

        List<IModule> out = Arrays.asList(modules);

        Event.invoke(new MidnightCoreLoadModulesEvent(this, out));

        if(mainConfig.has("disabled_modules", List.class)) {
            List<String> disabled = new ArrayList<>();
            for(Object o : mainConfig.getList("disabled_modules")) {
                if(!(o instanceof String)) continue;
                disabled.add((String) o);
            }

            for(int i = out.size() ; i > 0 ; i--) {
                IModule m = out.get(0);
                if(disabled.contains(m.getId())) {
                    out.remove(m);
                }
            }
        }
        return out.toArray(new IModule[0]);

    }

    public static MidnightCore getInstance() {
        return instance;
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static MidnightCoreAPI getAPI() {
        return api;
    }

}
