package org.wallentines.midnightcore.api.player;

import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.module.skin.Skinnable;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.UUID;

public interface MPlayer extends Skinnable {

    UUID getUUID();

    String getUsername();

    MComponent getName();

    Location getLocation();

    MItemStack getItemInMainHand();
    MItemStack getItemInOffhand();

    String getLocale();

    boolean isOffline();
    boolean hasPermission(String permission);
    boolean hasPermission(String permission, int permissionLevel);

    void sendMessage(MComponent component);
    void sendActionBar(MComponent component);
    void sendTitle(MComponent component, int fadeIn, int stay, int fadeOut);
    void sendSubtitle(MComponent component, int fadeIn, int stay, int fadeOut);
    void clearTitles();

    void playSound(Identifier soundId, String category, float volume, float pitch);

    void closeContainer();

    void executeCommand(String cmd);
    void sendChatMessage(String message);

    void giveItem(MItemStack item);
    void giveItem(MItemStack item, int slot);

    void teleport(Location newLoc);

}
