package org.wallentines.mcore;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.util.ConversionUtil;

public class SpigotCommandSender implements org.wallentines.mcore.CommandSender {

    private final CommandSender sender;

    public SpigotCommandSender(CommandSender sender) {
        this.sender = sender;
    }

    public CommandSender getSender() {
        return sender;
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public boolean hasPermission(String permission, int defaultOpLevel) {
        boolean perm = sender.hasPermission(permission);

        if(!perm && sender instanceof Player) {
            return Adapter.INSTANCE.get().hasOpLevel((Player) sender, defaultOpLevel);
        }

        return perm;
    }

    @Override
    public void sendSuccess(Component component, boolean log) {
        if (sender instanceof Player) {
            Adapter.INSTANCE.get().sendMessage((Player) sender, component);
        } else {
            sender.sendMessage(component.toLegacyText());
        }
    }

    @Override
    public void sendFailure(Component component) {
        sendSuccess(component, false);
    }

    @Override
    public Location getLocation() {

        if (sender instanceof Player) {
            return new SpigotPlayer(Server.RUNNING_SERVER.get(), (Player) sender).getLocation();
        } else {
            return ConversionUtil.toLocation(sender.getServer().getWorlds().get(0).getSpawnLocation());
        }
    }

    public static SpigotCommandSender of(CommandSender sender) {
        return new SpigotCommandSender(sender);
    }

    @Override
    public String getLanguage() {
        return sender instanceof Player
                ? new SpigotPlayer(Server.RUNNING_SERVER.get(), (Player) sender).getLanguage()
                : "en_us";
    }
}
