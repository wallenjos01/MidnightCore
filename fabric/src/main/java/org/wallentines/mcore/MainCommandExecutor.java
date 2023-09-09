package org.wallentines.mcore;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.WrappedComponent;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.midnightlib.registry.Identifier;

public class MainCommandExecutor {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("mcore")
                .requires(Permissions.require(MidnightCoreAPI.MOD_ID + ".command", 4))
                .executes(MainCommandExecutor::emptyCommand)
                .then(Commands.literal("module")
                    .then(Commands.literal("load")
                        .then(Commands.argument("module", ResourceLocationArgument.id())
                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(ServerModule.REGISTRY.getIds().stream().map(ConversionUtil::toResourceLocation), builder))
                            .executes(MainCommandExecutor::moduleLoadCommand)
                        )
                    )
                    .then(Commands.literal("unload")
                        .then(Commands.argument("module", ResourceLocationArgument.id())
                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(((Server) ((Server) ctx.getSource().getServer())).getModuleManager().getLoadedModuleIds().stream().map(ConversionUtil::toResourceLocation), builder))
                            .executes(MainCommandExecutor::moduleUnloadCommand)
                        )
                    )
                    .then(Commands.literal("reload")
                        .then(Commands.literal("all")
                            .executes(ctx -> moduleReloadCommand(ctx, null))
                        )
                        .then(Commands.argument("module", ResourceLocationArgument.id())
                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(((Server) ((Server) ctx.getSource().getServer())).getModuleManager().getLoadedModuleIds().stream().map(ConversionUtil::toResourceLocation), builder))
                            .executes(ctx -> moduleReloadCommand(ctx, ConversionUtil.toIdentifier(ctx.getArgument("module", ResourceLocation.class))))
                        )
                    )
                    .then(Commands.literal("enable")
                        .then(Commands.argument("module", ResourceLocationArgument.id())
                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(ServerModule.REGISTRY.getIds().stream().map(ConversionUtil::toResourceLocation), builder))
                            .executes(MainCommandExecutor::moduleEnableCommand)
                        )
                    )
                    .then(Commands.literal("disable")
                        .then(Commands.argument("module", ResourceLocationArgument.id())
                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(ServerModule.REGISTRY.getIds().stream().map(ConversionUtil::toResourceLocation), builder))
                            .executes(MainCommandExecutor::moduleDisableCommand)
                        )
                    )
                    .then(Commands.literal("list")
                        .executes(MainCommandExecutor::moduleListCommand)
                    )
                )
                .then(Commands.literal("reload")
                        .executes(MainCommandExecutor::reloadCommand)
                )
        );

    }

    private static int emptyCommand(CommandContext<CommandSourceStack> ctx) {
        return MainCommand.executeMain(((Server) ctx.getSource().getServer()), cmp -> sendSuccess(ctx.getSource(), cmp));
    }

    private static int moduleLoadCommand(CommandContext<CommandSourceStack> ctx) {

        Identifier id = ConversionUtil.toIdentifier(ctx.getArgument("module", ResourceLocation.class));
        return MainCommand.executeLoadModule(((Server) ctx.getSource().getServer()), id, cmp -> sendSuccess(ctx.getSource(), cmp));
    }

    private static int moduleUnloadCommand(CommandContext<CommandSourceStack> ctx) {

        Identifier id = ConversionUtil.toIdentifier(ctx.getArgument("module", ResourceLocation.class));
        return MainCommand.executeUnloadModule(((Server) ctx.getSource().getServer()), id, cmp -> sendSuccess(ctx.getSource(), cmp));
    }

    private static int moduleReloadCommand(CommandContext<CommandSourceStack> ctx, Identifier module) {
        return MainCommand.executeReloadModule(((Server) ctx.getSource().getServer()), module, cmp -> sendSuccess(ctx.getSource(), cmp));
    }

    private static int moduleListCommand(CommandContext<CommandSourceStack> ctx) {

        return MainCommand.executeListModules(((Server) ctx.getSource().getServer()), cmp -> sendSuccess(ctx.getSource(), cmp));
    }

    private static int moduleEnableCommand(CommandContext<CommandSourceStack> ctx) {

        Identifier id = ConversionUtil.toIdentifier(ctx.getArgument("module", ResourceLocation.class));
        return MainCommand.executeEnableModule(((Server) ctx.getSource().getServer()), id, cmp -> sendSuccess(ctx.getSource(), cmp));
    }

    private static int moduleDisableCommand(CommandContext<CommandSourceStack> ctx) {

        Identifier id = ConversionUtil.toIdentifier(ctx.getArgument("module", ResourceLocation.class));
        return MainCommand.executeDisableModule(((Server) ctx.getSource().getServer()), id, cmp -> sendSuccess(ctx.getSource(), cmp));
    }

    private static int reloadCommand(CommandContext<CommandSourceStack> ctx) {

        return MainCommand.executeReload(cmp -> sendSuccess(ctx.getSource(), cmp));
    }


    private static void sendSuccess(CommandSourceStack stack, Component comp) {

        stack.sendSuccess(() -> WrappedComponent.resolved(comp, (Player) stack.getPlayer()), false);
    }


}
