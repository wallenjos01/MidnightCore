package me.m1dnightninja.midnightcore.fabric;

import me.m1dnightninja.midnightcore.api.config.ConfigRegistry;
import me.m1dnightninja.midnightcore.api.inventory.MInventoryGUI;
import me.m1dnightninja.midnightcore.api.text.MTimer;
import me.m1dnightninja.midnightcore.api.module.IModule;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.text.MScoreboard;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.common.MidnightCoreImpl;
import me.m1dnightninja.midnightcore.common.config.JsonConfigProvider;
import me.m1dnightninja.midnightcore.fabric.module.dimension.DimensionModule;
import me.m1dnightninja.midnightcore.fabric.module.playerdata.PlayerDataModule;
import me.m1dnightninja.midnightcore.fabric.module.savepoint.SavePointModule;
import me.m1dnightninja.midnightcore.fabric.module.skin.SkinModule;
import me.m1dnightninja.midnightcore.fabric.module.vanish.VanishModule;
import me.m1dnightninja.midnightcore.fabric.text.FabricTimer;
import me.m1dnightninja.midnightcore.fabric.module.dimension.EmptyGenerator;
import me.m1dnightninja.midnightcore.fabric.inventory.FabricItem;
import me.m1dnightninja.midnightcore.fabric.inventory.FabricInventoryGUI;
import me.m1dnightninja.midnightcore.fabric.module.lang.LangModule;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayerManager;
import me.m1dnightninja.midnightcore.fabric.text.FabricScoreboard;
import me.m1dnightninja.midnightcore.fabric.util.PermissionUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class MidnightCore implements ModInitializer {

    private static MidnightCore instance;
    private static MinecraftServer server;


    @Override
    public void onInitialize() {

        instance = this;
        Logger logger = LogManager.getLogger("MidnightCore");

        File configDirectory = Paths.get("config/MidnightCore").toFile();
        if(!configDirectory.exists() && !configDirectory.mkdirs()) {
            logger.warn("Unable to create config directory!");
            return;
        }

        ConfigRegistry.INSTANCE.registerProvider(JsonConfigProvider.INSTANCE);
        ConfigRegistry.INSTANCE.setDefaultProvider(JsonConfigProvider.INSTANCE);

        // Default modules
        final List<IModule> modules = new ArrayList<>(5);
        modules.add(new SkinModule());
        modules.add(new LangModule());
        modules.add(new DimensionModule());
        modules.add(new SavePointModule());
        modules.add(new PlayerDataModule());
        modules.add(new VanishModule());

        // Find sub-mods
        List<MidnightCoreModInitializer> inits = FabricLoader.getInstance().getEntrypoints("midnightcore:mod", MidnightCoreModInitializer.class);
        for(MidnightCoreModInitializer init : inits) {

            init.onInitialize();

            Collection<IModule> mods = init.getModules();
            if(mods != null) {
                modules.addAll(mods);
            }
        }

        // Create API
        MidnightCoreAPI api = new MidnightCoreImpl(
                new FabricPlayerManager(),
                FabricItem::new,
                configDirectory,
                modules.toArray(new IModule[0])
        ) {
            @Override
            public MTimer createTimer(MComponent text, int seconds, boolean countUp, MTimer.TimerCallback cb) {
                return new FabricTimer(text, seconds, countUp, cb);
            }

            @Override
            public MInventoryGUI createInventoryGUI(MComponent title) {
                return new FabricInventoryGUI(title);
            }

            @Override
            public MScoreboard createScoreboard(String id, MComponent title) {
                return new FabricScoreboard(id, title);
            }

            @Override
            public void executeConsoleCommand(String cmd) {
                getServer().getCommands().performCommand(getServer().createCommandSourceStack(), cmd);
            }
            @Override
            public String getGameVersion() {

                Optional<ModContainer> cont = FabricLoader.getInstance().getModContainer("minecraft");
                if(cont.isPresent()) {
                    return cont.get().getMetadata().getVersion().getFriendlyString();
                }

                return "1.17.1";
            }
        };

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

            CommandRegistrationCallback.EVENT.register(((commandDispatcher, b) -> PermissionUtil.registerVanillaPermissions(commandDispatcher)));
        }

        FabricInventoryGUI.registerEvents(this);
    }

    public static MidnightCore getInstance() {
        return instance;
    }

    public static MinecraftServer getServer() {
        return server;
    }

}
