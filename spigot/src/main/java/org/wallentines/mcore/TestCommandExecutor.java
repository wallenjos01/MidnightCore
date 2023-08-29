package org.wallentines.mcore;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.util.TestUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestCommandExecutor extends BukkitCommand {

    public TestCommandExecutor() {
        super("mcoretest");
        this.description = "MidnightCore test command";
        this.setPermission(MidnightCoreAPI.MOD_ID + ".command.test");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {

        if(!sender.hasPermission(MidnightCoreAPI.MOD_ID + ".command.test")) {
            return false;
        }

        executeTest(sender, args);
        return true;
    }

    private void executeTest(CommandSender sender, String[] args) {

        if(!(sender instanceof org.bukkit.entity.Player)) {
            sender.sendMessage(ChatColor.RED + "Only players may use that command!");
            return;
        }

        SpigotPlayer player = new SpigotPlayer(Server.RUNNING_SERVER.get(), (Player) sender);

        if(args.length == 0) {
            TestUtil.cmd(player);
            return;
        }

        switch (args[0]) {
            case "gui" -> TestUtil.guiCmd(player);
            case "equip" -> TestUtil.equipCmd(player);
            case "scoreboard" -> TestUtil.scoreboardCmd(player);
            case "skin" -> TestUtil.skinCmd(player);
            case "save" -> TestUtil.saveCmd(player);
            case "load" -> TestUtil.loadCmd(player);
        }
    }


    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        List<String> options = getTestSubcommands(args);

        if(args.length == 0) {
            return options;
        }

        return options.stream().filter(arg -> arg.startsWith(args[args.length - 1])).collect(Collectors.toList());
    }


    private List<String> getTestSubcommands(String[] args) {

        if(args.length > 1) return new ArrayList<>();
        return Arrays.asList("gui", "equip", "scoreboard", "skin", "save", "load");
    }
}
