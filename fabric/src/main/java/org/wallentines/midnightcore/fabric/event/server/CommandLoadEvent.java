package org.wallentines.midnightcore.fabric.event.server;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import org.wallentines.midnightlib.event.Event;

public class CommandLoadEvent extends Event {

    private final CommandDispatcher<CommandSourceStack> dispatcher;
    private final Commands.CommandSelection commandSelection;

    public CommandLoadEvent(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection commandSelection) {
        this.dispatcher = dispatcher;
        this.commandSelection = commandSelection;
    }

    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return dispatcher;
    }

    public Commands.CommandSelection getCommandSelection() {
        return commandSelection;
    }

    public boolean isDedicatedServer() {
        return commandSelection == Commands.CommandSelection.DEDICATED;
    }
}
