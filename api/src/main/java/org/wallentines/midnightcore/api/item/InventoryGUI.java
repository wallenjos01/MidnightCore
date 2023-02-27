package org.wallentines.midnightcore.api.item;

import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface InventoryGUI {

    int getPageSize();

    void setPageSize(int pageSize);

    int pageCount();

    MItemStack getItem(int slot);

    void removeItem(int slot);

    GUIAction getAction(int slot);

    void setItem(int slot, MItemStack stack, GUIAction action);

    void open(MPlayer player, int page);

    void close(MPlayer player);

    void update();

    void update(MPlayer player);

    int getPage(MPlayer player);

    int getPageOffset(int page);

    void addCloseCallback(Consumer<MPlayer> player);

    interface GUIAction {

        void onClick(ClickType type, MPlayer player);
    }

    enum ClickType {
        LEFT,
        RIGHT,
        SHIFT_LEFT,
        SHIFT_RIGHT,
        MIDDLE,
        DOUBLE,
        THROW,
        THROW_ALL,
        NUMBER_KEY
    }

    interface Factory {
        InventoryGUI create(MComponent title);
    }

}
