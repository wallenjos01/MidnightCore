package org.wallentines.midnightcore.api.item;

import org.wallentines.midnightcore.api.player.MPlayer;

import java.util.function.Consumer;

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

    int getPage(MPlayer player);

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

}
