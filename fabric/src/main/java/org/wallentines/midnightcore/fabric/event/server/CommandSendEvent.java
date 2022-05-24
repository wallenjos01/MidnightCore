package org.wallentines.midnightcore.fabric.event.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import org.wallentines.midnightlib.event.Event;

public class CommandSendEvent extends Event {

    private final CommandSourceStack source;
    private final String command;
    private final CommandDispatcher<CommandSourceStack> dispatcher;

    private boolean cancelled;

    public CommandSendEvent(CommandSourceStack source, String command, CommandDispatcher<CommandSourceStack> dispatcher) {
        this.source = source;
        this.command = command;
        this.dispatcher = dispatcher;
    }

    public CommandSourceStack getSource() {
        return source;
    }

    public String getCommand() {
        return command;
    }

    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return dispatcher;
    }

    public String getCommandName() {

        int index = command.indexOf(" ");
        return index == -1 ? command.substring(1) : command.substring(1, index);
    }

    public CommandNode<CommandSourceStack> getCommandNode() {

        return dispatcher.getRoot().getChild(getCommandName());
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
