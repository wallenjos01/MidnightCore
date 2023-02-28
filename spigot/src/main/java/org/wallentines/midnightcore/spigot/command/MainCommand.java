package org.wallentines.midnightcore.spigot.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.Registries;
import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.api.text.CustomPlaceholderInline;
import org.wallentines.midnightcore.api.text.LangProvider;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.spigot.util.CommandUtil;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final List<String> subcommands = List.of("module", "reload");
    private final List<String> moduleSubcommands = List.of("load", "unload", "enable", "disable", "list");

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(!commandSender.hasPermission(Constants.makeNode("command"))) {
            return List.of();
        }

        String lastArg;
        Stream<String> commands = null;
        if(args.length <= 1) { // First argument

            commands = subcommands.stream();
            if(args.length == 1) {
                lastArg = args[0];
            } else {
                lastArg = "";
            }

        } else if(args.length == 2 && args[0].equals("module")) { // Second Argument

            commands = moduleSubcommands.stream();
            lastArg = args[1];

        } else if(args.length == 3 && args[0].equals("module") && moduleSubcommands.contains(args[1]) && !args[1].equals("list")) {

            commands = Registries.MODULE_REGISTRY.getIds().stream().map(Identifier::toString);
            lastArg = args[2];

        } else {

            lastArg = "";
        }

        if(commands == null) return List.of();

        return commands.filter(cmd -> cmd.startsWith(lastArg)).collect(Collectors.toList());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(!sender.hasPermission(Constants.makeNode("command"))) {
            return false;
        }

        MServer server = MidnightCoreAPI.getRunningServer();
        if(server == null) return false;

        LangProvider provider = server.getMidnightCore().getLangProvider();

        switch (args.length) {
            case 0: {
                CommandUtil.sendFeedback(sender, provider, "command.main");
                break;
            }
            case 1: {
                switch(args[0]) {
                    case "module": {
                        sendUsage(sender, label + " module", "[<module>]", provider, moduleSubcommands);
                        break;
                    }
                    case "reload": {
                        long elapsed = System.currentTimeMillis();

                        MidnightCoreAPI api = MidnightCoreAPI.getInstance();
                        if(api != null) api.reload();

                        elapsed = System.currentTimeMillis() - elapsed;

                        CommandUtil.sendFeedback(sender, provider, "command.reload", CustomPlaceholderInline.create("time", elapsed + ""));
                        break;
                    }
                }
            }
            case 2:
                if(!args[0].equals("module")) {
                    sendUsage(sender, label, "", provider, subcommands);
                    break;
                }
                if(!args[1].equals("list")) {
                    sendUsage(sender, label + " module", "[<module>]", provider, moduleSubcommands);
                    break;
                }

                CommandUtil.sendFeedback(sender, provider, "command.module.list.header");

                ModuleManager<MServer, ServerModule> manager = server.getModuleManager();
                for (ModuleInfo<MServer, ServerModule> info : Registries.MODULE_REGISTRY) {

                    CustomPlaceholderInline cp = CustomPlaceholderInline.create("module_id", info.getId().toString());

                    if (manager.isModuleLoaded(info.getId())) {
                        CommandUtil.sendFeedback(sender, provider,"command.module.list.enabled", cp);
                    } else {
                        CommandUtil.sendFeedback(sender, provider,"command.module.list.disabled", cp);
                    }
                }

                break;

            case 3:
                if(!args[0].equals("module")) {
                    sendUsage(sender, label, "", provider, subcommands);
                    return true;
                }

                Identifier moduleId = Identifier.parseOrDefault(args[2], MidnightCoreAPI.DEFAULT_NAMESPACE);

                switch (args[1]) {
                    case "load": {

                        ModuleInfo<MServer, ServerModule> info = Registries.MODULE_REGISTRY.get(moduleId);
                        if(info == null) {
                            CommandUtil.sendFeedback(sender, provider, "command.error.module_not_found");
                            break;
                        }

                        if(server.getModuleManager().isModuleLoaded(moduleId)) {
                            CommandUtil.sendFeedback(sender, provider, "command.error.module_loaded");
                            break;
                        }

                        server.getModuleManager().loadModule(info, server, server.getModuleConfig().getRoot().getSection("modules").getOrCreateSection(moduleId.toString()));
                        CommandUtil.sendFeedback(sender, provider, "command.module.loaded", CustomPlaceholderInline.create("module_id", moduleId.toString()));
                        break;
                    }
                    case "unload": {

                        if(!server.getModuleManager().isModuleLoaded(moduleId)) {
                            CommandUtil.sendFeedback(sender, provider, "command.error.module_not_loaded");
                            break;
                        }
                        server.getModuleManager().unloadModule(moduleId);
                        CommandUtil.sendFeedback(sender, provider, "command.module.unloaded", CustomPlaceholderInline.create("module_id", moduleId.toString()));

                        break;
                    }
                    case "enable": {

                        server.getModuleConfig().getRoot().getOrCreateSection(moduleId.toString()).set("enabled", true);
                        server.getModuleConfig().save();
                        CommandUtil.sendFeedback(sender, provider, "command.module.enabled", CustomPlaceholderInline.create("module_id", moduleId.toString()));

                        break;
                    }
                    case "disable": {

                        server.getModuleConfig().getRoot().getOrCreateSection(moduleId.toString()).set("enabled", false);
                        server.getModuleConfig().save();
                        CommandUtil.sendFeedback(sender, provider, "command.module.disabled", CustomPlaceholderInline.create("module_id", moduleId.toString()));

                        break;
                    }
                    default: {
                        sendUsage(sender, label + " module", "[<module>]", provider, moduleSubcommands);
                        break;
                    }
                }
        }


        return true;
    }

    private void sendUsage(CommandSender sender, String label, String after, LangProvider provider, List<String> args) {

        StringBuilder usage = new StringBuilder("/");
        usage.append(label);
        usage.append(" <");
        for(int i = 0 ; i < args.size() ; i++) {
            if(i > 0) {
                usage.append("/");
            }
            usage.append(args.get(i));
        }
        usage.append("> ").append(after);

        CommandUtil.sendFeedback(sender, provider, "command.error.usage", CustomPlaceholderInline.create("usage", usage.toString()));

    }

}
