package org.wallentines.mcore;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.wallentines.mcore.util.TestUtil;

public class TestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players may use that command!");
            return true;
        }

        SpigotPlayer player = new SpigotPlayer(Server.RUNNING_SERVER.get(), (Player) sender);

        if(args.length == 0) {
            TestUtil.cmd(player);
            return true;
        }

        if(args[0].equals("gui")) {
            TestUtil.guiCmd(player);
            return true;
        }

        if(args[0].equals("equip")) {
            TestUtil.equipCmd(player);
            return true;
        }

        if(args[0].equals("scoreboard")) {
            TestUtil.scoreboardCmd(player);
            return true;
        }

        return true;
    }
}
