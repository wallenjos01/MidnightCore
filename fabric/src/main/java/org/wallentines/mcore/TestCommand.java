package org.wallentines.mcore;

import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.wallentines.mcore.util.TestUtil;

public class TestCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("mcoretest")
            .requires(Permissions.require("midnightcore.command.test", 4))
            .executes(ctx -> {
                Player pl = ctx.getSource().getPlayerOrException();
                TestUtil.cmd(pl);
                return 1;
            })
            .then(Commands.literal("skin")
                    .executes(ctx -> {
                        Player pl = ctx.getSource().getPlayerOrException();
                        TestUtil.skinCmd(pl);
                        return 1;
                    })
            )
            .then(Commands.literal("save")
                    .executes(ctx -> {
                        Player pl = ctx.getSource().getPlayerOrException();
                        TestUtil.saveCmd(pl);
                        return 1;
                    })
            )
            .then(Commands.literal("load")
                    .executes(ctx -> {
                        Player pl = ctx.getSource().getPlayerOrException();
                        TestUtil.loadCmd(pl);
                        return 1;
                    })
            )
            .then(Commands.literal("gui")
                    .executes(ctx -> {
                        Player pl = ctx.getSource().getPlayerOrException();
                        TestUtil.guiCmd(pl);
                        return 1;
                    })
            )
            .then(Commands.literal("scoreboard")
                    .executes(ctx -> {
                        Player pl = ctx.getSource().getPlayerOrException();
                        TestUtil.scoreboardCmd(pl);
                        return 1;
                    })
            )
        );

    }

}
