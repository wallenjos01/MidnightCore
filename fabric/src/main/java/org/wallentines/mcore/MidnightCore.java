package org.wallentines.mcore;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.SharedConstants;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import org.wallentines.mcore.item.ItemStack;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.skin.FabricSkinModule;
import org.wallentines.mcore.skin.SkinModule;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mcore.util.TestUtil;
import org.wallentines.mdcfg.codec.JSONCodec;

import java.nio.file.Path;

public class MidnightCore implements ModInitializer {


    public static final Path DATA_FOLDER = Path.of("config", MidnightCoreAPI.MOD_ID);

    @Override
    public void onInitialize() {

        MidnightCoreAPI.FILE_CODEC_REGISTRY.registerFileCodec(JSONCodec.fileCodec());

        ServerLifecycleEvents.SERVER_STARTING.register(Server.RUNNING_SERVER::set);
        ServerLifecycleEvents.SERVER_STARTED.register(Server.START_EVENT::invoke);
        ServerLifecycleEvents.SERVER_STOPPING.register(Server.STOP_EVENT::invoke);
        ServerLifecycleEvents.SERVER_STOPPED.register(srv -> Server.RUNNING_SERVER.reset());

        ServerModule.REGISTRY.register(SkinModule.ID, FabricSkinModule.MODULE_INFO);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("mcoretest")
                .executes(ctx -> {

                    Player pl = ctx.getSource().getPlayerOrException();
                    TestUtil.cmd(pl);

                    return 1;
                })
                .then(Commands.literal("skin")
                    .executes(ctx -> {

                        Player pl = ctx.getSource().getPlayerOrException();
                        TestUtil.skinCmd(pl);
                        return 1;
                    })
                )
            );
        });

    }

    static {

        GameVersion.CURRENT_VERSION.set(new GameVersion(SharedConstants.getCurrentVersion().getId(), SharedConstants.getProtocolVersion()));

        ItemStack.FACTORY.set((id, count, tag, data) -> {

            if(data != -1) {
                throw new IllegalStateException("ItemStack data value requested for an unsupported version!");
            }

            Item it = BuiltInRegistries.ITEM.get(ConversionUtil.toResourceLocation(id));
            ItemStack out = (ItemStack) (Object) new net.minecraft.world.item.ItemStack(it, count);
            out.setTag(tag);
            return out;
        });

        Player.registerPlaceholders(PlaceholderManager.INSTANCE);

    }
}
