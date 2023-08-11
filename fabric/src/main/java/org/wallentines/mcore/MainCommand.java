package org.wallentines.mcore;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import org.wallentines.mcore.lang.CustomPlaceholder;
import org.wallentines.mcore.lang.LangContent;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.WrappedComponent;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mcore.util.ModuleUtil;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

public class MainCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("mcore")
                .requires(Permissions.require(MidnightCoreAPI.MOD_ID + ".command", 4))
                .executes(MainCommand::emptyCommand)
                .then(Commands.literal("module")
                    .then(Commands.literal("load")
                        .then(Commands.argument("module", ResourceLocationArgument.id())
                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(ServerModule.REGISTRY.getIds().stream().map(ConversionUtil::toResourceLocation), builder))
                            .executes(MainCommand::moduleLoadCommand)
                        )
                    )
                    .then(Commands.literal("unload")
                        .then(Commands.argument("module", ResourceLocationArgument.id())
                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(ctx.getSource().getServer().getModuleManager().getLoadedModuleIds().stream().map(ConversionUtil::toResourceLocation), builder))
                            .executes(MainCommand::moduleUnloadCommand)
                        )
                    )
//                    .then(Commands.literal("enable")
//                        .then(Commands.argument("module", ResourceLocationArgument.id())
//                            .suggests((ctx, builder) -> (SharedSuggestionProvider.suggestResource(Registries.MODULE_REGISTRY.getIds().stream().map(ConversionUtil::toResourceLocation), builder)))
//                            .executes(MainCommand::moduleEnableCommand)
//                        )
//                    )
//                    .then(Commands.literal("disable")
//                        .then(Commands.argument("module", ResourceLocationArgument.id())
//                            .suggests((ctx, builder) -> (SharedSuggestionProvider.suggestResource(Registries.MODULE_REGISTRY.getIds().stream().map(ConversionUtil::toResourceLocation), builder)))
//                            .executes(MainCommand::moduleDisableCommand)
//                        )
//                    )
                    .then(Commands.literal("list")
                        .executes(MainCommand::moduleListCommand)
                    )
//                )
//                .then(Commands.literal("reload")
//                        .executes(MainCommand::reloadCommand)
                )
        );

    }

    private static int emptyCommand(CommandContext<CommandSourceStack> ctx) {

        MidnightCoreServer mcore = MidnightCoreServer.INSTANCE.get();
        sendSuccess(ctx.getSource(), LangContent.component(mcore.getLangManager(), "command.main", ctx.getSource().getServer()));

        return 1;
    }

    private static int moduleLoadCommand(CommandContext<CommandSourceStack> ctx) {

        Server server = ctx.getSource().getServer();
        Identifier id = ConversionUtil.toIdentifier(ctx.getArgument("module", ResourceLocation.class));

        MidnightCoreServer mcore = MidnightCoreServer.INSTANCE.get();

        if(server.getModuleManager().isModuleLoaded(id)) {

            sendSuccess(ctx.getSource(), LangContent.component(mcore.getLangManager(), "command.module.already_loaded", new CustomPlaceholder("module_id", id.toString())));
            return 1;
        }

        ModuleInfo<Server, ServerModule> info = ServerModule.REGISTRY.get(id);
        if(info == null) {
            sendFailure(ctx.getSource(), LangContent.component(mcore.getLangManager(), "error.module_not_found", new CustomPlaceholder("module_id", id.toString())));
            return 0;
        }

        if(ModuleUtil.loadModule(server.getModuleManager(), info, server, server.getModuleConfig())) {
            sendSuccess(ctx.getSource(), LangContent.component(mcore.getLangManager(), "command.module.loaded", new CustomPlaceholder("module_id", id.toString())));
            return 2;
        }

        sendFailure(ctx.getSource(), LangContent.component(mcore.getLangManager(), "error.module_load_failed", new CustomPlaceholder("module_id", id.toString())));
        return 0;
    }

    private static int moduleUnloadCommand(CommandContext<CommandSourceStack> ctx) {

        Server server = ctx.getSource().getServer();
        Identifier id = ConversionUtil.toIdentifier(ctx.getArgument("module", ResourceLocation.class));

        MidnightCoreServer mcore = MidnightCoreServer.INSTANCE.get();

        if(!server.getModuleManager().isModuleLoaded(id)) {
            sendSuccess(ctx.getSource(), LangContent.component(mcore.getLangManager(), "command.module.already_unloaded", new CustomPlaceholder("module_id", id.toString())));
            return 1;
        }

        server.getModuleManager().unloadModule(id);
        sendSuccess(ctx.getSource(), LangContent.component(mcore.getLangManager(), "command.module.unloaded", new CustomPlaceholder("module_id", id.toString())));
        return 2;
    }

    private static int moduleListCommand(CommandContext<CommandSourceStack> ctx) {

        Server server = ctx.getSource().getServer();

        MidnightCoreServer mcore = MidnightCoreServer.INSTANCE.get();
        sendSuccess(ctx.getSource(), LangContent.component(mcore.getLangManager(), "command.module.list.header"));

        for(Identifier id : server.getModuleManager().getLoadedModuleIds()) {

            String state = server.getModuleManager().isModuleLoaded(id) ? "loaded" : "unloaded";
            sendSuccess(
                    ctx.getSource(),
                    LangContent.component(
                            mcore.getLangManager(),
                            "command.module.list.entry",
                            new CustomPlaceholder("module_id", id.toString()),
                            new CustomPlaceholder("state", LangContent.component(
                                    mcore.getLangManager(),
                                    "module.state." + state))
                    )
            );
        }

        return 1;
    }

    private static void sendSuccess(CommandSourceStack stack, Component component) {
        stack.sendSuccess(() -> WrappedComponent.resolved(component, stack.getPlayer()), false);
    }

    private static void sendFailure(CommandSourceStack stack, Component component) {
        stack.sendFailure(WrappedComponent.resolved(component, stack.getPlayer()));
    }


}
