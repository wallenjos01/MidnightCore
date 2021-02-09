package me.m1dnightninja.midnightcore.fabric;

import me.m1dnightninja.midnightcore.api.AbstractTimer;
import me.m1dnightninja.midnightcore.api.ImplDelegate;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.fabric.api.InventoryGUI;
import me.m1dnightninja.midnightcore.fabric.api.Timer;
import me.m1dnightninja.midnightcore.fabric.api.event.MidnightCoreInitEvent;
import me.m1dnightninja.midnightcore.fabric.api.event.MidnightCoreLoadGeneratorsEvent;
import me.m1dnightninja.midnightcore.fabric.dimension.EmptyGenerator;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.module.DimensionModule;
import me.m1dnightninja.midnightcore.fabric.module.LangModule;
import me.m1dnightninja.midnightcore.fabric.module.SavePointModule;
import me.m1dnightninja.midnightcore.fabric.module.SkinModule;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.util.Map;

public class MidnightCore implements ModInitializer {

    private static MidnightCoreAPI api;

    private static MinecraftServer server;

    @Override
    public void onInitialize() {

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
                new Logger(LogManager.getLogger()),
                delegate,
                new SkinModule(),
                new LangModule(),
                new DimensionModule(),
                new SavePointModule()
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

    public static MinecraftServer getServer() {
        return server;
    }

    public static MidnightCoreAPI getAPI() {
        return api;
    }

}
