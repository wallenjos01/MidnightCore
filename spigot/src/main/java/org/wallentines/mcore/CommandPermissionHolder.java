package org.wallentines.mcore;

import org.bukkit.command.CommandSender;

public class CommandPermissionHolder implements PermissionHolder {

    private final CommandSender sender;

    public CommandPermissionHolder(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public boolean hasPermission(String permission, int defaultOpLevel) {
        return sender.hasPermission(permission);
    }

    public static CommandPermissionHolder of(CommandSender sender) {
        return new CommandPermissionHolder(sender);
    }
}
