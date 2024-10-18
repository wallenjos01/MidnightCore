package org.wallentines.mcore;

import org.wallentines.mcore.lang.PlaceholderContext;
import org.wallentines.mcore.lang.UnresolvedComponent;
import org.wallentines.mcore.text.Component;
import org.wallentines.midnightlib.types.Singleton;

public interface InventoryGUI {

    /**
     * Sets the item and click event at the given index of the GUI.
     * @param index The slot index.
     * @param itemStack The item to put at that index.
     * @param event The click event to invoke when a player clicks at the index.
     */
    void setItem(int index, ItemStack itemStack, SingleInventoryGUI.ClickEvent event);

    /**
     * Sets the item and click event at the given index of the GUI. The item will be resolved with respect to each player.
     * @param index The slot index.
     * @param itemStack The item to put at that index.
     * @param event The click event to invoke when a player clicks at the index.
     */
    void setItem(int index, UnresolvedItemStack itemStack, SingleInventoryGUI.ClickEvent event);

    /**
     * Removes the item and click event at the given index of the GUI.
     * @param index The index to clear.
     */
    void clearItem(int index);

    /**
     * Gets the number of rows in the GUI.
     * @return The number of rows.
     */
    int rows();

    /**
     * Gets the number of slots in the GUI.
     * @return The number of slots.
     */
    int size();

    /**
     * Gets the index of the first empty slot in the GUI.
     * @return The index of the first empty slot.
     */
    int firstEmpty();

    /**
     * Gets the index of the last filled slot in the GUI.
     * @return The index of the last filled slot.
     */
    int lastItem();

    /**
     * Removes all items and actions from the GUI
     */
    void clear();

    /**
     * Updates the menu
     */
    void update();

    /**
     * Opens the GUI for the given player
     */
    void open(Player player);

    /**
     * Closes the GUI for the given player
     */
    void close(Player player);

    /**
     * Closes the GUI for all players currently viewing it.
     */
    void closeAll();

    /**
     * Moves the viewers of this inventory GUI to another GUI
     * @param other The other GUI
     */
    void moveViewers(InventoryGUI other);

    Singleton<Factory> FACTORY = new Singleton<>();

    static SingleInventoryGUI create(Component title, int size) {
        return create(UnresolvedComponent.completed(title), size);
    }

    static SingleInventoryGUI create(UnresolvedComponent title, int size) {
        return create(title, size, new PlaceholderContext());
    }

    static SingleInventoryGUI create(Component title, int size, PlaceholderContext context) {
        return create(UnresolvedComponent.completed(title), size, context);
    }

    static SingleInventoryGUI create(UnresolvedComponent title, int size, PlaceholderContext ctx) {

        int rows = size / 9;
        int partialRows = size % 9;
        if(rows == 0 || partialRows > 0) rows++;

        return FACTORY.get().build(title, rows, ctx);
    }

    static PagedInventoryGUI createPaged(Component title, PagedInventoryGUI.SizeProvider sizeProvider) {
        return new PagedInventoryGUI(UnresolvedComponent.completed(title), sizeProvider, 0, new PlaceholderContext());
    }

    static PagedInventoryGUI createPaged(UnresolvedComponent title, PagedInventoryGUI.SizeProvider sizeProvider) {
        return new PagedInventoryGUI(title, sizeProvider, 0, new PlaceholderContext());
    }

    static PagedInventoryGUI createPaged(Component title, PagedInventoryGUI.SizeProvider sizeProvider, int size) {
        return new PagedInventoryGUI(UnresolvedComponent.completed(title), sizeProvider, size, new PlaceholderContext());
    }

    static PagedInventoryGUI createPaged(UnresolvedComponent title, PagedInventoryGUI.SizeProvider sizeProvider, int size) {
        return new PagedInventoryGUI(title, sizeProvider, size, new PlaceholderContext());
    }

    static PagedInventoryGUI createPaged(Component title, PagedInventoryGUI.SizeProvider sizeProvider, PlaceholderContext ctx) {
        return new PagedInventoryGUI(UnresolvedComponent.completed(title), sizeProvider, 0, ctx);
    }

    static PagedInventoryGUI createPaged(UnresolvedComponent title, PagedInventoryGUI.SizeProvider sizeProvider, PlaceholderContext ctx) {
        return new PagedInventoryGUI(title, sizeProvider, 0, ctx);
    }

    static PagedInventoryGUI createPaged(Component title, PagedInventoryGUI.SizeProvider sizeProvider, int size, PlaceholderContext ctx) {
        return new PagedInventoryGUI(UnresolvedComponent.completed(title), sizeProvider, size, ctx);
    }

    static PagedInventoryGUI createPaged(UnresolvedComponent title, PagedInventoryGUI.SizeProvider sizeProvider, int size, PlaceholderContext ctx) {
        return new PagedInventoryGUI(title, sizeProvider, size, ctx);
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

    interface ClickEvent {

        void execute(Player player, ClickType type);

    }

    interface Factory {

        SingleInventoryGUI build(UnresolvedComponent title, int rows, PlaceholderContext ctx);
    }

}
