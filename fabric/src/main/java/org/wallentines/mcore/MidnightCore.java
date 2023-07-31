package org.wallentines.mcore;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.SharedConstants;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import org.wallentines.mcore.extension.FabricServerExtensionModule;
import org.wallentines.mcore.extension.ServerExtensionModule;
import org.wallentines.mcore.item.ItemStack;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.messaging.FabricServerMessagingModule;
import org.wallentines.mcore.messaging.ServerMessagingModule;
import org.wallentines.mcore.savepoint.FabricSavepoint;
import org.wallentines.mcore.savepoint.SavepointModule;
import org.wallentines.mcore.session.FabricSessionModule;
import org.wallentines.mcore.session.SessionModule;
import org.wallentines.mcore.skin.FabricSkinModule;
import org.wallentines.mcore.skin.SkinModule;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mcore.util.TestUtil;
import org.wallentines.mdcfg.codec.JSONCodec;

public class MidnightCore implements ModInitializer {


    @Override
    public void onInitialize() {

        MidnightCoreAPI.FILE_CODEC_REGISTRY.registerFileCodec(JSONCodec.fileCodec());


        ServerLifecycleEvents.SERVER_STARTING.register(Server.RUNNING_SERVER::set);
        ServerLifecycleEvents.SERVER_STARTED.register(Server.START_EVENT::invoke);
        ServerLifecycleEvents.SERVER_STOPPING.register(Server.STOP_EVENT::invoke);
        ServerLifecycleEvents.SERVER_STOPPED.register(srv -> Server.RUNNING_SERVER.reset());


        ServerModule.REGISTRY.register(SkinModule.ID, FabricSkinModule.MODULE_INFO);
        ServerModule.REGISTRY.register(SavepointModule.ID, FabricSavepoint.MODULE_INFO);
        ServerModule.REGISTRY.register(SessionModule.ID, FabricSessionModule.MODULE_INFO);
        ServerModule.REGISTRY.register(ServerMessagingModule.ID, FabricServerMessagingModule.MODULE_INFO);
        ServerModule.REGISTRY.register(ServerExtensionModule.ID, FabricServerExtensionModule.MODULE_INFO);


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
                .then(Commands.literal("save")
                        .executes(ctx -> {
                            Player pl = ctx.getSource().getPlayerOrException();
                            TestUtil.saveCmd(pl);
                            return 1;
                        })
                )
                .then(Commands.literal("load")
                        .executes(ctx -> {
                            Player pl = ctx.getSource().getPlayerOrException();
                            TestUtil.loadCmd(pl);
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
