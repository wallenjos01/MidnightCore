package org.wallentines.midnightcore.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.api.text.CustomPlaceholderInline;
import org.wallentines.midnightcore.api.text.LangProvider;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.api.Registries;
import org.wallentines.midnightcore.fabric.util.CommandUtil;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;

public class MainCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("mcore")
                .requires(Permissions.require(Constants.makeNode("command"), 4))
                .executes(MainCommand::emptyCommand)
                .then(Commands.literal("module")
                    .then(Commands.literal("load")
                        .then(Commands.argument("module", ResourceLocationArgument.id())
                            .suggests((ctx, builder) -> (SharedSuggestionProvider.suggestResource(Registries.MODULE_REGISTRY.getIds().stream().map(ConversionUtil::toResourceLocation), builder)))
                            .executes(MainCommand::moduleLoadCommand)
                        )
                    )
                    .then(Commands.literal("unload")
                        .then(Commands.argument("module", ResourceLocationArgument.id())
                            .suggests((ctx, builder) -> (SharedSuggestionProvider.suggestResource(Registries.MODULE_REGISTRY.getIds().stream().map(ConversionUtil::toResourceLocation), builder)))
                            .executes(MainCommand::moduleUnloadCommand)
                        )
                    )
                    .then(Commands.literal("enable")
                        .then(Commands.argument("module", ResourceLocationArgument.id())
                            .suggests((ctx, builder) -> (SharedSuggestionProvider.suggestResource(Registries.MODULE_REGISTRY.getIds().stream().map(ConversionUtil::toResourceLocation), builder)))
                            .executes(MainCommand::moduleEnableCommand)
                        )
                    )
                    .then(Commands.literal("disable")
                        .then(Commands.argument("module", ResourceLocationArgument.id())
                            .suggests((ctx, builder) -> (SharedSuggestionProvider.suggestResource(Registries.MODULE_REGISTRY.getIds().stream().map(ConversionUtil::toResourceLocation), builder)))
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

    private static int emptyCommand(CommandContext<CommandSourceStack> context) {

        LangProvider prov = getLangProvider();
        context.getSource().sendSuccess(ConversionUtil.toComponent(prov.getMessage("command.main", "en_us")), false);
        return 1;
    }

    private static int moduleLoadCommand(CommandContext<CommandSourceStack> context) {

        MServer server = MidnightCoreAPI.getRunningServer();
        if(server == null) {
            throw new CommandRuntimeException(Component.literal("There is no server running!"));
        }

        LangProvider prov = server.getMidnightCore().getLangProvider();

        Identifier id = ConversionUtil.toIdentifier(context.getArgument("module", ResourceLocation.class));
        ModuleInfo<MServer, ServerModule> info = Registries.MODULE_REGISTRY.get(id);
        if(info == null) {
            CommandUtil.sendCommandFailure(context, prov, "command.error.module_not_found");
            return 0;
        }

        if(server.getModuleManager().isModuleLoaded(id)) {
            CommandUtil.sendCommandFailure(context, prov, "command.error.module_loaded");
            return 0;
        }

        server.getModuleManager().loadModule(info, server, server.getModuleConfig().getRoot().getSection("modules").getOrCreateSection(id.toString()));
        CommandUtil.sendCommandSuccess(context, prov, false, "command.module.loaded", CustomPlaceholderInline.create("module_id", id.toString()));

        return 1;
    }

    private static int moduleUnloadCommand(CommandContext<CommandSourceStack> context) {

        MServer server = MidnightCoreAPI.getRunningServer();
        if(server == null) return 0;

        Identifier id = ConversionUtil.toIdentifier(context.getArgument("module", ResourceLocation.class));
        if(!server.getModuleManager().isModuleLoaded(id)) {
            CommandUtil.sendCommandFailure(context, getLangProvider(), "command.error.module_not_loaded");
            return 0;
        }

        server.getModuleManager().unloadModule(id);
        CommandUtil.sendCommandSuccess(context, getLangProvider(), false, "command.module.unloaded", CustomPlaceholderInline.create("module_id", id.toString()));

        return 1;
    }

    private static int moduleEnableCommand(CommandContext<CommandSourceStack> context) {

        MServer server = MidnightCoreAPI.getRunningServer();
        if(server == null) return 0;

        LangProvider prov = getLangProvider();

        Identifier id = ConversionUtil.toIdentifier(context.getArgument("module", ResourceLocation.class));
        server.getModuleConfig().getRoot().getOrCreateSection(id.toString()).set("enabled", true);
        server.getModuleConfig().save();
        CommandUtil.sendCommandSuccess(context, prov, false, "command.module.enabled", CustomPlaceholderInline.create("module_id", id.toString()));

        return 1;
    }

    private static int moduleDisableCommand(CommandContext<CommandSourceStack> context) {

        MServer server = MidnightCoreAPI.getRunningServer();
        if(server == null) return 0;

        LangProvider prov = getLangProvider();

        Identifier id = ConversionUtil.toIdentifier(context.getArgument("module", ResourceLocation.class));
        server.getModuleConfig().getRoot().getOrCreateSection(id.toString()).set("enabled", false);
        server.getModuleConfig().save();
        CommandUtil.sendCommandSuccess(context, prov, false, "command.module.disabled", CustomPlaceholderInline.create("module_id", id.toString()));

        return 1;
    }

    private static int moduleListCommand(CommandContext<CommandSourceStack> context) {

        MServer server = MidnightCoreAPI.getRunningServer();
        if(server == null) return 0;

        LangProvider prov = getLangProvider();

        CommandUtil.sendCommandSuccess(context, prov,false, "command.module.list.header");

        ModuleManager<MServer, ServerModule> manager = server.getModuleManager();
        for (ModuleInfo<MServer, ServerModule> info : Registries.MODULE_REGISTRY) {

            CustomPlaceholderInline cp = CustomPlaceholderInline.create("module_id", info.getId().toString());

            if (manager.isModuleLoaded(info.getId())) {
                CommandUtil.sendCommandSuccess(context, prov,false, "command.module.list.enabled", cp);
            } else {
                CommandUtil.sendCommandSuccess(context, prov,false, "command.module.list.disabled", cp);
            }
        }
        return 1;
    }

    private static int reloadCommand(CommandContext<CommandSourceStack> context) {

        LangProvider prov = getLangProvider();

        long elapsed = System.currentTimeMillis();

        MidnightCoreAPI api = MidnightCoreAPI.getInstance();
        if(api != null) api.reload();

        elapsed = System.currentTimeMillis() - elapsed;

        CommandUtil.sendCommandSuccess(context, prov, false, "command.reload", CustomPlaceholderInline.create("time", elapsed + ""));
        return (int) elapsed;
    }

    private static LangProvider getLangProvider() throws CommandRuntimeException {

        MidnightCoreAPI api = MidnightCoreAPI.getInstance();
        if(api == null) {
            throw new CommandRuntimeException(Component.literal("MidnightCoreAPI is not loaded!"));
        }
        return api.getLangProvider();
    }


}