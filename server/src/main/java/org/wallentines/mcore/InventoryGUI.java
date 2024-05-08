package org.wallentines.mcore;

import org.jetbrains.annotations.NotNull;
import org.wallentines.mcore.text.Component;
import org.wallentines.midnightlib.types.Either;
import org.wallentines.midnightlib.types.Singleton;

import java.util.HashMap;
import java.util.UUID;

public abstract class InventoryGUI {

    protected final int size;
    protected final Entry[] items;
    protected final Component title;

    protected static final HashMap<UUID, InventoryGUI> OPEN_GUIS = new HashMap<>();

    public static final Singleton<Factory> FACTORY = new Singleton<>();

    protected InventoryGUI(Component title, int rows) {

        if(rows > 6 || rows < 1) {
            throw new IllegalArgumentException("Cannot create inventory GUI with " + rows + " rows!");
        }

        this.size = rows * 9;
        this.items = new Entry[size];
        this.title = title;
    }

    public void setItem(int index, @NotNull ItemStack itemStack, ClickEvent event) {
        this.items[index] = new Entry(itemStack, event);
    }

    public void setItem(int index, @NotNull UnresolvedItemStack itemStack, ClickEvent event) {
        this.items[index] = new Entry(itemStack, event);
    }

    public void clearItem(int index) {
        this.items[index] = null;
    }

    public int firstEmpty() {
        for(int i = 0 ; i < items.length ; i++) {
            if(items[i] == null) return i;
        }
        return -1;
    }

    public int lastItem() {
        for(int i = items.length ; i > 0 ; i--) {
            if(items[i - 1] != null) return i;
        }
        return -1;
    }

    public void clear() {
        int last = lastItem();
        for(int i = 0 ; i < last ; i++) {
            items[i] = null;
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

        if(OPEN_GUIS.remove(player.getUUID()) == this) {
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

    public enum ClickType {
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

    public interface ClickEvent {

        void execute(Player player, ClickType type);

    }

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

    public interface Factory {

        InventoryGUI build(Component title, int rows);
    }

}
