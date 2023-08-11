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
import org.wallentines.mcore.text.WrappedComponent;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mcore.util.ModuleUtil;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.codec.FileWrapper;
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
                    .then(Commands.literal("reload")
                        .then(Commands.literal("all")
                            .executes(ctx -> moduleReloadCommand(ctx, null))
                        )
                        .then(Commands.argument("module", ResourceLocationArgument.id())
                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(ctx.getSource().getServer().getModuleManager().getLoadedModuleIds().stream().map(ConversionUtil::toResourceLocation), builder))
                            .executes(ctx -> moduleReloadCommand(ctx, ConversionUtil.toIdentifier(ctx.getArgument("module", ResourceLocation.class))))
                        )
                    )
                    .then(Commands.literal("enable")
                        .then(Commands.argument("module", ResourceLocationArgument.id())
                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(ServerModule.REGISTRY.getIds().stream().map(ConversionUtil::toResourceLocation), builder))
                            .executes(MainCommand::moduleEnableCommand)
                        )
                    )
                    .then(Commands.literal("disable")
                        .then(Commands.argument("module", ResourceLocationArgument.id())
                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(ServerModule.REGISTRY.getIds().stream().map(ConversionUtil::toResourceLocation), builder))
                            .executes(MainCommand::moduleDisableCommand)
                        )
                    )
                    .then(Commands.literal("list")
                        .executes(MainCommand::moduleListCommand)
                    )
                )
                .then(Commands.literal("reload")
                        .executes(MainCommand::reloadCommand)
                )
        );

    }

    private static int emptyCommand(CommandContext<CommandSourceStack> ctx) {

        sendSuccess(ctx.getSource(),"command.main", ctx.getSource().getServer());
        return 1;
    }

    private static int moduleLoadCommand(CommandContext<CommandSourceStack> ctx) {

        Server server = ctx.getSource().getServer();
        Identifier id = ConversionUtil.toIdentifier(ctx.getArgument("module", ResourceLocation.class));

        if(server.getModuleManager().isModuleLoaded(id)) {

            sendSuccess(ctx.getSource(), "command.module.already_loaded", CustomPlaceholder.inline("module_id", id));
            return 1;
        }

        ModuleInfo<Server, ServerModule> info = ServerModule.REGISTRY.get(id);
        if(info == null) {
            sendFailure(ctx.getSource(), "error.module_not_found", CustomPlaceholder.inline("module_id", id));
            return 0;
        }

        if(ModuleUtil.loadModule(server.getModuleManager(), info, server, server.getModuleConfig())) {
            sendSuccess(ctx.getSource(), "command.module.loaded", CustomPlaceholder.inline("module_id", id));
            return 2;
        }

        sendFailure(ctx.getSource(), "error.module_load_failed", CustomPlaceholder.inline("module_id", id));
        return 0;
    }

    private static int moduleUnloadCommand(CommandContext<CommandSourceStack> ctx) {

        Server server = ctx.getSource().getServer();
        Identifier id = ConversionUtil.toIdentifier(ctx.getArgument("module", ResourceLocation.class));

        if(!server.getModuleManager().isModuleLoaded(id)) {
            sendSuccess(ctx.getSource(), "command.module.already_unloaded", CustomPlaceholder.inline("module_id", id));
            return 1;
        }

        server.getModuleManager().unloadModule(id);
        sendSuccess(ctx.getSource(), "command.module.unloaded", CustomPlaceholder.inline("module_id", id));
        return 2;
    }

    private static int moduleReloadCommand(CommandContext<CommandSourceStack> ctx, Identifier module) {

        Server server = ctx.getSource().getServer();

        if(module == null) {

            server.getModuleManager().reloadAll(server.getModuleConfig().getRoot().asSection(), server, ServerModule.REGISTRY);
            int loaded = server.getModuleManager().getCount();

            sendSuccess(ctx.getSource(), "command.module.reload.all", CustomPlaceholder.inline("count", loaded));
            return loaded;

        } else {

            Identifier id = ConversionUtil.toIdentifier(ctx.getArgument("module", ResourceLocation.class));
            ModuleInfo<Server, ServerModule> info = ServerModule.REGISTRY.get(id);
            if(info == null) {
                sendFailure(ctx.getSource(), "error.module_not_found", CustomPlaceholder.inline("module_id", id.toString()));
                return 0;
            }

            if(ModuleUtil.reloadModule(server.getModuleManager(), info, server, server.getModuleConfig())) {
                sendSuccess(ctx.getSource(), "command.module.reload", CustomPlaceholder.inline("module_id", id.toString()));
                return 1;
            }

            sendFailure(ctx.getSource(), "error.module_load_failed", CustomPlaceholder.inline("module_id", id.toString()));
            return 0;
        }
    }

    private static int moduleListCommand(CommandContext<CommandSourceStack> ctx) {

        Server server = ctx.getSource().getServer();

        MidnightCoreServer mcore = MidnightCoreServer.INSTANCE.get();
        sendSuccess(ctx.getSource(), "command.module.list.header", server);

        for(Identifier id : ServerModule.REGISTRY.getIds()) {

            String state = server.getModuleManager().isModuleLoaded(id) ? "loaded" : "unloaded";
            sendSuccess(
                ctx.getSource(),
                    "command.module.list.entry",
                    server,
                    CustomPlaceholder.inline("module_id", id),
                    CustomPlaceholder.of("state", () -> LangContent.component(
                        mcore.getLangManager(),
                        "module.state." + state))
            );
        }

        return 1;
    }

    private static int moduleEnableCommand(CommandContext<CommandSourceStack> ctx) {

        Server server = ctx.getSource().getServer();
        Identifier id = ConversionUtil.toIdentifier(ctx.getArgument("module", ResourceLocation.class));

        ModuleInfo<Server, ServerModule> info = ServerModule.REGISTRY.get(id);
        if(info == null) {
            sendFailure(ctx.getSource(), "error.module_not_found", CustomPlaceholder.inline("module_id", id));
            return 0;
        }

        FileWrapper<ConfigObject> obj = server.getModuleConfig();
        obj.getRoot().asSection().getOrCreateSection(id.toString()).set("enabled", true);

        sendSuccess(ctx.getSource(), "command.module.enabled", CustomPlaceholder.inline("module_id", id));

        return 1;
    }

    private static int moduleDisableCommand(CommandContext<CommandSourceStack> ctx) {

        Server server = ctx.getSource().getServer();
        Identifier id = ConversionUtil.toIdentifier(ctx.getArgument("module", ResourceLocation.class));

        ModuleInfo<Server, ServerModule> info = ServerModule.REGISTRY.get(id);
        if(info == null) {
            sendFailure(ctx.getSource(), "error.module_not_found", CustomPlaceholder.inline("module_id", id));
            return 0;
        }

        FileWrapper<ConfigObject> obj = server.getModuleConfig();
        obj.getRoot().asSection().getOrCreateSection(id.toString()).set("enabled", false);

        sendSuccess(ctx.getSource(), "command.module.disabled", CustomPlaceholder.inline("module_id", id));

        return 1;
    }

    private static int reloadCommand(CommandContext<CommandSourceStack> ctx) {

        long start = System.currentTimeMillis();

        MidnightCoreServer mcore = MidnightCoreServer.INSTANCE.get();
        mcore.getLangManager().reload();

        long elapsed = System.currentTimeMillis() - start;
        sendSuccess(ctx.getSource(), "command.reload", CustomPlaceholder.inline("time", elapsed));

        return 1;
    }


    private static void sendSuccess(CommandSourceStack stack, String key, Object... args) {

        stack.sendSuccess(() -> {
            MidnightCoreServer mcore = MidnightCoreServer.INSTANCE.get();
            return WrappedComponent.resolved(LangContent.component(mcore.getLangManager(), key, args), stack.getPlayer());
        }, false);
    }

    private static void sendFailure(CommandSourceStack stack, String key, Object... args) {

        MidnightCoreServer mcore = MidnightCoreServer.INSTANCE.get();
        stack.sendFailure(WrappedComponent.resolved(LangContent.component(mcore.getLangManager(), key, args), stack.getPlayer()));
    }


}
