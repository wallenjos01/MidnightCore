package org.wallentines.mcore;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.wallentines.midnightlib.registry.Identifier;

public class MainCommandExecutor {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("mcore")
                .requires(Permissions.require(MidnightCoreAPI.MOD_ID + ".command", 4))
                .executes(MainCommandExecutor::emptyCommand)
                .then(Commands.literal("module")
                    .then(Commands.literal("load")
                        .then(Commands.argument("module", IdentifierArgument.MCORE)
                            .suggests((ctx, builder) -> IdentifierArgument.MCORE.suggest(ServerModule.REGISTRY.getIds().stream(), builder))
                            .executes(MainCommandExecutor::moduleLoadCommand)
                        )
                    )
                    .then(Commands.literal("unload")
                        .then(Commands.argument("module", IdentifierArgument.MCORE)
                            .suggests((ctx, builder) -> IdentifierArgument.MCORE.suggest(ctx.getSource().getServer().getModuleManager().getLoadedModuleIds().stream(), builder))
                            .executes(MainCommandExecutor::moduleUnloadCommand)
                        )
                    )
                    .then(Commands.literal("reload")
                        .then(Commands.literal("all")
                            .executes(ctx -> moduleReloadCommand(ctx, null))
                        )
                        .then(Commands.argument("module", IdentifierArgument.MCORE)
                            .suggests((ctx, builder) -> IdentifierArgument.MCORE.suggest(ctx.getSource().getServer().getModuleManager().getLoadedModuleIds().stream(), builder))
                            .executes(ctx -> moduleReloadCommand(ctx, ctx.getArgument("module", Identifier.class)))
                        )
                    )
                    .then(Commands.literal("enable")
                        .then(Commands.argument("module", IdentifierArgument.MCORE)
                            .suggests((ctx, builder) -> IdentifierArgument.MCORE.suggest(ServerModule.REGISTRY.getIds().stream(), builder))
                            .executes(MainCommandExecutor::moduleEnableCommand)
                        )
                    )
                    .then(Commands.literal("disable")
                        .then(Commands.argument("module", IdentifierArgument.MCORE)
                            .suggests((ctx, builder) -> IdentifierArgument.MCORE.suggest(ctx.getSource().getServer().getModuleManager().getLoadedModuleIds().stream(), builder))
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
        return MainCommand.executeMain(ctx.getSource().getServer(), ctx.getSource());
    }

    private static int moduleLoadCommand(CommandContext<CommandSourceStack> ctx) {

        Identifier id = ctx.getArgument("module", Identifier.class);
        return MainCommand.executeLoadModule(ctx.getSource().getServer(), id, ctx.getSource());
    }

    private static int moduleUnloadCommand(CommandContext<CommandSourceStack> ctx) {

        Identifier id = ctx.getArgument("module", Identifier.class);
        return MainCommand.executeUnloadModule(ctx.getSource().getServer(), id, ctx.getSource());
    }

    private static int moduleReloadCommand(CommandContext<CommandSourceStack> ctx, Identifier module) {
        return MainCommand.executeReloadModule(ctx.getSource().getServer(), module, ctx.getSource());
    }

    private static int moduleListCommand(CommandContext<CommandSourceStack> ctx) {

        return MainCommand.executeListModules(ctx.getSource().getServer(), ctx.getSource());
    }

    private static int moduleEnableCommand(CommandContext<CommandSourceStack> ctx) {

        Identifier id = ctx.getArgument("module", Identifier.class);
        return MainCommand.executeEnableModule(ctx.getSource().getServer(), id, ctx.getSource());
    }

    private static int moduleDisableCommand(CommandContext<CommandSourceStack> ctx) {

        Identifier id = ctx.getArgument("module", Identifier.class);
        return MainCommand.executeDisableModule(ctx.getSource().getServer(), id, ctx.getSource());
    }

    private static int reloadCommand(CommandContext<CommandSourceStack> ctx) {

        return MainCommand.executeReload(ctx.getSource());
    }

}
