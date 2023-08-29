package org.wallentines.mcore;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ComponentResolver;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainCommandExecutor extends BukkitCommand {

    public MainCommandExecutor() {
        super("mcore");
        this.description = "Main MidnightCore command";
        this.setPermission(MidnightCoreAPI.MOD_ID + ".command");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {

        if(!sender.hasPermission(MidnightCoreAPI.MOD_ID + ".command")) {
            return false;
        }

        executeMain(sender, args);
        return true;
    }

    private void executeMain(CommandSender sender, String[] args) {

        Server server = Server.RUNNING_SERVER.get();

        if(args.length == 0) {
            MainCommand.executeMain(server, cmp -> sendMessage(sender, cmp));
            return;
        }

        if(args[0].equals("module")) {

            if(args.length == 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /mcore module <load/unload/reload/enable/disable/list>");
                return;
            }

            switch (args[1]) {
                case "load":
                    if(args.length == 2) {
                        sender.sendMessage(ChatColor.RED + "Usage: /mcore module load <[module]>");
                        return;
                    }
                    MainCommand.executeLoadModule(server, Identifier.parseOrDefault(MidnightCoreAPI.MOD_ID, args[2]), cmp -> sendMessage(sender, cmp));
                    return;
                case "unload":
                    if(args.length == 2) {
                        sender.sendMessage(ChatColor.RED + "Usage: /mcore module unload <[module]>");
                        return;
                    }
                    MainCommand.executeUnloadModule(server, Identifier.parseOrDefault(MidnightCoreAPI.MOD_ID, args[2]), cmp -> sendMessage(sender, cmp));
                    return;
                case "reload":
                    if(args.length == 2) {
                        sender.sendMessage(ChatColor.RED + "Usage: /mcore module reload <[module]/all>");
                        return;
                    }
                    if(args[2].equals("all")) {
                        MainCommand.executeReloadModule(server, null, cmp -> sendMessage(sender, cmp));
                    } else {
                        MainCommand.executeReloadModule(server, Identifier.parseOrDefault(MidnightCoreAPI.MOD_ID, args[2]), cmp -> sendMessage(sender, cmp));
                    }
                    return;
                case "enable":
                    if(args.length == 2) {
                        sender.sendMessage(ChatColor.RED + "Usage: /mcore module enable <[module]>");
                        return;
                    }
                    MainCommand.executeEnableModule(server, Identifier.parseOrDefault(MidnightCoreAPI.MOD_ID, args[2]), cmp -> sendMessage(sender, cmp));
                    return;
                case "disable":
                    if(args.length == 2) {
                        sender.sendMessage(ChatColor.RED + "Usage: /mcore module disable <[module]>");
                        return;
                    }
                    MainCommand.executeEnableModule(server, Identifier.parseOrDefault(MidnightCoreAPI.MOD_ID, args[2]), cmp -> sendMessage(sender, cmp));
                    return;
                case "list":
                    MainCommand.executeListModules(server, cmp -> sendMessage(sender, cmp));
            }

        } else if(args[0].equals("reload")) {

            MainCommand.executeReload(cmp -> sendMessage(sender, cmp));
        }

    }


    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {

        List<String> options = getMainSubcommands(args);

        if(args.length == 0) {
            return options;
        }

        return options.stream().filter(arg -> arg.startsWith(args[args.length - 1])).collect(Collectors.toList());
    }


    private List<String> getMainSubcommands(String[] args) {

        if(args.length == 1) {
            return Arrays.asList("module", "reload");
        }

        if(args.length == 2 && args[0].equals("module")) {
            return Arrays.asList("load", "unload", "reload", "enable", "disable", "list");
        }

        if(args.length == 3 && args[0].equals("module")) {
            if(Arrays.asList("load", "unload", "reload", "enable", "disable").contains(args[1])) {
                List<String> out = new ArrayList<>();
                for(Identifier id : Server.RUNNING_SERVER.get().getModuleManager().getLoadedModuleIds()) {
                    out.add(id.toString());
                }
                if(args[1].equals("reload")) {
                    out.add("all");
                }
                return out;
            }
        }

        return new ArrayList<>();
    }

    private void sendMessage(CommandSender sender, Component cmp) {

        if(sender instanceof Player) {
            Adapter.INSTANCE.get().sendMessage((Player) sender, ComponentResolver.resolveComponent(cmp, new SpigotPlayer(Server.RUNNING_SERVER.get(), (Player) sender)));
        } else {
            sender.sendMessage(ComponentResolver.resolveComponent(cmp, null).toLegacyText());
        }
    }
}
