package org.wallentines.mcore;

import org.jetbrains.annotations.NotNull;
import org.wallentines.mcore.lang.UnresolvedComponent;
import org.wallentines.mdcfg.Tuples;
import org.wallentines.midnightlib.types.Either;

import java.util.HashMap;
import java.util.UUID;

public abstract class SingleInventoryGUI implements InventoryGUI {

    protected final int size;
    protected final Entry[] items;
    protected final UnresolvedComponent title;

    protected static final HashMap<UUID, SingleInventoryGUI> OPEN_GUIS = new HashMap<>();

    protected SingleInventoryGUI(UnresolvedComponent title, int rows) {

        if(rows > 6 || rows < 1) {
            throw new IllegalArgumentException("Cannot create inventory GUI with " + rows + " rows!");
        }

        this.size = rows * 9;
        this.items = new Entry[size];
        this.title = title;
    }

    /**
     * Sets the item and click event at the given index of the GUI.
     * @param index The slot index.
     * @param itemStack The item to put at that index.
     * @param event The click event to invoke when a player clicks at the index.
     */
    public void setItem(int index, @NotNull ItemStack itemStack, ClickEvent event) {
        this.items[index] = new Entry(itemStack, event);
    }

    /**
     * Sets the item and click event at the given index of the GUI. The item will be resolved with respect to each player.
     * @param index The slot index.
     * @param itemStack The item to put at that index.
     * @param event The click event to invoke when a player clicks at the index.
     */
    public void setItem(int index, @NotNull UnresolvedItemStack itemStack, ClickEvent event) {
        this.items[index] = new Entry(itemStack, event);
    }

    /**
     * Removes the item and click event at the given index of the GUI.
     * @param index The index to clear.
     */
    public void clearItem(int index) {
        this.items[index] = null;
    }

    /**
     * Gets the number of rows in the GUI.
     * @return The number of rows.
     */
    public int rows() {
        return size / 9;
    }

    /**
     * Gets the number of slots in the GUI.
     * @return The number of slots.
     */
    public int size() {
        return size;
    }

    /**
     * Gets the index of the first empty slot in the GUI.
     * @return The index of the first empty slot.
     */
    public int firstEmpty() {
        for(int i = 0 ; i < items.length ; i++) {
            if(items[i] == null) return i;
        }
        return -1;
    }

    /**
     * Gets the index of the last filled slot in the GUI.
     * @return The index of the last filled slot.
     */
    public int lastItem() {
        for(int i = items.length ; i > 0 ; i--) {
            if(items[i - 1] != null) return i;
        }
        return -1;
    }

    /**
     * Removes all items and actions from the GUI
     */
    public void clear() {
        int last = lastItem();
        for(int i = 0 ; i < last ; i++) {
            items[i] = null;
        }
    }

    /**
     * Makes an exact copy of this GUI.
     * @return A copy of the GUI.
     */
    public SingleInventoryGUI copy() {

        SingleInventoryGUI other = InventoryGUI.create(title, rows());
        System.arraycopy(items, 0, other.items, 0, size);
        other.update();

        return other;
    }

    /**
     * Makes an exact copy of this GUI with the given title.
     * @param title The new title of the GUI.
     * @return A copy of the GUI.
     */
    public SingleInventoryGUI copy(UnresolvedComponent title) {

        SingleInventoryGUI other = InventoryGUI.create(title, rows());
        System.arraycopy(items, 0, other.items, 0, size);
        other.update();

        return other;
    }

    /**
     * Changes the size of the GUI, copying if necessary..
     * @param rows The new amount of rows in the GUI.
     * @return A menu with the given number of rows.
     */
    public SingleInventoryGUI withSize(int rows) {
        if(rows == rows()) return this;

        int newSize = rows * 9;

        SingleInventoryGUI other = InventoryGUI.create(title, rows);
        System.arraycopy(items, 0, other.items, 0, Math.min(size, newSize));
        other.update();

        return other;
    }

    public SingleInventoryGUI append(SingleInventoryGUI other) {

        SingleInventoryGUI out = InventoryGUI.create(title, rows() + other.rows());
        System.arraycopy(items, 0, out.items, 0, size);
        System.arraycopy(other.items, 0, out.items, size, other.size);
        out.update();

        return out;
    }

    public SingleInventoryGUI prepend(SingleInventoryGUI other) {

        SingleInventoryGUI out = InventoryGUI.create(title, rows() + other.rows());
        System.arraycopy(other.items, 0, out.items, 0, other.size);
        System.arraycopy(items, 0, out.items, other.size, size);
        out.update();

        return out;
    }

    public SingleInventoryGUI subGUI(int begin, int end) {
        int size = end - begin;
        int rows = size / 9;
        int partialRows = size % 9;
        if(partialRows > 0 || rows == 0) rows++;

        SingleInventoryGUI out = InventoryGUI.create(title, rows);
        System.arraycopy(items, begin, out.items, 0, size);
        out.update();

        return out;
    }

    public Tuples.T2<SingleInventoryGUI, SingleInventoryGUI> split(int index) {
        return new Tuples.T2<>(
                subGUI(0, index),
                subGUI(index, size)
        );
    }

    public void clearRow(int row) {
        int rowIndex = row * 9;
        clear(rowIndex, rowIndex + 9);
    }

    public void clear(int start, int end) {
        for(int i = start ; i < end ; i++) {
            clearItem(i);
        }
    }

    public void onClick(int index, Player player, ClickType type) {
        if(this.items[index] == null || this.items[index].event == null) return;
        this.items[index].event.execute(player, type);
    }

    public void open(Player player) {

        closeMenu(player);
        doOpen(player);

        OPEN_GUIS.put(player.getUUID(), this);
    }

    public void close(Player player) {

        if(OPEN_GUIS.get(player.getUUID()) == this) {
            OPEN_GUIS.remove(player.getUUID());
            doClose(player);
        }
    }

    public static void closeMenu(Player player) {
        if(OPEN_GUIS.containsKey(player.getUUID())) {
            OPEN_GUIS.get(player.getUUID()).close(player);
        }
    }

    public abstract void update();
    protected abstract void doOpen(Player player);
    protected abstract void doClose(Player player);


    public static class Entry {

        private final Either<ItemStack, UnresolvedItemStack> item;
        private final ClickEvent event;

        public Entry(ItemStack item, ClickEvent event) {
            this.item = Either.left(item);
            this.event = event;
        }

        public Entry(UnresolvedItemStack item, ClickEvent event) {
            this.item = Either.right(item);
            this.event = event;
        }

        public ItemStack getItem(Player player) {
            return item.leftOrGet(r -> r.resolve(player));
        }

        public ClickEvent getEvent() {
            return event;
        }
    }


}
