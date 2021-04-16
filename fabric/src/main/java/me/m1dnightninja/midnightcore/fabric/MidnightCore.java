package me.m1dnightninja.midnightcore.fabric;

import me.m1dnightninja.midnightcore.api.AbstractTimer;
import me.m1dnightninja.midnightcore.api.IModule;
import me.m1dnightninja.midnightcore.api.ImplDelegate;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.common.JsonConfigProvider;
import me.m1dnightninja.midnightcore.fabric.api.InventoryGUI;
import me.m1dnightninja.midnightcore.fabric.api.MidnightCoreModInitializer;
import me.m1dnightninja.midnightcore.fabric.api.PermissionHelper;
import me.m1dnightninja.midnightcore.fabric.api.Timer;
import me.m1dnightninja.midnightcore.fabric.dimension.EmptyGenerator;
import me.m1dnightninja.midnightcore.fabric.module.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class MidnightCore implements ModInitializer {

    private static MidnightCore instance;
    private static MinecraftServer server;

    private File configDirectory;

    @Override
    public void onInitialize() {

        instance = this;
        Logger logger = new Logger(LogManager.getLogger());

        configDirectory = Paths.get("config/MidnightCore").toFile();
        if(!configDirectory.exists() && !configDirectory.mkdirs()) {
            logger.warn("Unable to create config directory!");
            return;
        }

        // Default modules
        final List<IModule> modules = new ArrayList<>(5);
        modules.add(new SkinModule());
        modules.add(new LangModule());
        modules.add(new DimensionModule());
        modules.add(new SavePointModule());
        modules.add(new PlayerDataModule());

        // Find sub-mods
        List<MidnightCoreModInitializer> inits = FabricLoader.getInstance().getEntrypoints("midnightcore:mod", MidnightCoreModInitializer.class);
        for(MidnightCoreModInitializer init : inits) {

            init.onInitialize();

            Collection<IModule> mods = init.getModules();
            if(mods != null) {
                modules.addAll(mods);
            }
        }

        // TODO: Replace this system with a more sensible one
        ImplDelegate delegate = new ImplDelegate() {
            @Override
            public Timer createTimer(String text, int seconds, boolean countUp, AbstractTimer.TimerCallback cb) {
                return new Timer(text, seconds, countUp, cb);
            }

            @Override
            public InventoryGUI createInventoryGUI(String title) {
                return new InventoryGUI(title);
            }

            @Override
            public boolean hasPermission(UUID u, String permission) {
                return PermissionHelper.check(u, permission);
            }
        };

        // Create API
        MidnightCoreAPI api = new MidnightCoreAPI(
                logger,
                delegate,
                new JsonConfigProvider(),
                configDirectory,
                modules.toArray(new IModule[0])
        );

        // Tell sub-mods that the api was created
        for(MidnightCoreModInitializer init : inits) {
            init.onAPICreated(this, api);
        }


        // Find a class that is or extends DimensionModule
        DimensionModule dimensionModule = api.getModule(DimensionModule.class);
        if(dimensionModule != null) {

            // When the server begins starting
            ServerLifecycleEvents.SERVER_STARTING.register((minecraftServer) -> {
                server = minecraftServer;

                // Create a list of custom chunk generators
                HashMap<ResourceLocation, ChunkGenerator> generators = new HashMap<>();
                generators.put(new ResourceLocation("midnightcore", "empty"), new EmptyGenerator(new ResourceLocation("forest")));

                // Get custom generators from child mods
                for (MidnightCoreModInitializer init : inits) {
                    Map<ResourceLocation, ChunkGenerator> gens = init.getGenerators();

                    if (gens != null) {
                        generators.putAll(gens);
                    }
                }

                // Register all generators
                for (Map.Entry<ResourceLocation, ChunkGenerator> ent : generators.entrySet()) {
                    dimensionModule.registerGeneratorType(ent.getKey(), ent.getValue());
                }
            });
        }

        // Register vanilla permissions
        if(api.getMainConfig().has("vanilla_permissions", Boolean.class) && api.getMainConfig().getBoolean("vanilla_permissions")) {

            CommandRegistrationCallback.EVENT.register(((commandDispatcher, b) -> PermissionHelper.registerVanillaPermissions(commandDispatcher)));
        }


        InventoryGUI.registerEvents(this);


    }

    public File getConfigDirectory() {
        return configDirectory;
    }

    public static MidnightCore getInstance() {
        return instance;
    }

    public static MinecraftServer getServer() {
        return server;
    }

}
