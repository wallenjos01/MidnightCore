package org.wallentines.midnightcore.spigot.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MStyle;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.midnightcore.api.text.TextColor;
import org.wallentines.midnightcore.spigot.player.SpigotPlayer;
import org.wallentines.midnightlib.registry.Identifier;

public class TestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if(args.length == 0 || !(commandSender instanceof Player)) return false;

        if(args[0].equals("item")) {

            Player p = (Player) commandSender;

            MItemStack is = MItemStack.Builder.of(new Identifier("minecraft", "diamond_sword")).withName(new MTextComponent("Hello").withStyle(new MStyle().withColor(TextColor.fromRGBI(6)))).build();
            MPlayer mpl = SpigotPlayer.wrap(p);

            MidnightCoreAPI.getLogger().warn(MItemStack.toNBT(is.getTag()));

            mpl.giveItem(is);

        }

        return true;
    }
}
