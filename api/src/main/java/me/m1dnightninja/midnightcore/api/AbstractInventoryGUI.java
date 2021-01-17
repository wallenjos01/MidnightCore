package me.m1dnightninja.midnightcore.api;

import java.util.HashMap;
import java.util.UUID;

public abstract class AbstractInventoryGUI<I> {

    protected HashMap<Integer, Entry> entries = new HashMap<>();
    protected HashMap<UUID, Integer> players = new HashMap<>();

    protected final String title;

    protected AbstractInventoryGUI(String title) {
        this.title = title;
    }

    public final void removeItem(int slot) {
        entries.remove(slot);
    }

    public final I getItem(int slot) {
        if(!entries.containsKey(slot)) return null;
        return entries.get(slot).item;
    }

    public final ClickAction getAction(int slot) {
        if(!entries.containsKey(slot)) return null;
        return entries.get(slot).action;
    }

    public final void setItem(I item, int slot, ClickAction action) {
        Entry ent = new Entry(item, slot, action);
        entries.put(slot, ent);
    }

    public final void open(UUID u, int page) {
        players.put(u, page);
        onOpened(u, page);
    }

    public final void close(UUID u) {
        if(!players.containsKey(u)) return;
        players.remove(u);
        onClosed(u);
    }

    public final int getPlayerPage(UUID u) {
        return players.get(u);
    }

    protected abstract void onClosed(UUID u);
    protected abstract void onOpened(UUID u, int page);

    protected class Entry {
        public I item;
        public int slot;
        public ClickAction action;

        public Entry(I item, int slot, ClickAction action) {
            this.item = item;
            this.slot = slot;
            this.action = action;
        }
    }

    public interface ClickAction {
        void onClick(ClickType type);
    }

    public enum ClickType {
        LEFT,
        RIGHT,
        SHIFT_LEFT,
        SHIFT_RIGHT,
        MIDDLE,
        DOUBLE_CLICK,
        THROW,
        THROW_ALL,
        NUMBER_KEY
    }

}
