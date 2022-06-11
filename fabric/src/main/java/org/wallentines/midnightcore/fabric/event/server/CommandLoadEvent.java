package org.wallentines.midnightcore.fabric.event.server;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import org.wallentines.midnightlib.event.Event;

public class CommandLoadEvent extends Event {

    private final CommandDispatcher<CommandSourceStack> dispatcher;
    private final Commands.CommandSelection commandSelection;
    private final CommandBuildContext buildContext;

    public CommandLoadEvent(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection commandSelection, CommandBuildContext buildContext) {
        this.dispatcher = dispatcher;
        this.commandSelection = commandSelection;
        this.buildContext = buildContext;
    }

    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return dispatcher;
    }

    public Commands.CommandSelection getCommandSelection() {
        return commandSelection;
    }

    public CommandBuildContext getBuildContext() {
        return buildContext;
    }

    public boolean isDedicatedServer() {
        return commandSelection == Commands.CommandSelection.DEDICATED;
    }
}
