package me.m1dnightninja.midnightcore.api;

import java.util.HashMap;
import java.util.UUID;

public abstract class AbstractInventoryGUI<I> {

    protected final HashMap<Integer, Entry> entries = new HashMap<>();
    protected final HashMap<UUID, Integer> players = new HashMap<>();
    protected final String title;

    protected static final HashMap<UUID, AbstractInventoryGUI<?>> openGuis = new HashMap<>();


    protected AbstractInventoryGUI(String title) {
        this.title = title;
    }

    public final void removeItem(int slot) {
        this.entries.remove(slot);
    }

    public final I getItem(int slot) {
        if (!this.entries.containsKey(slot)) {
            return null;
        }
        return this.entries.get(slot).item;
    }

    public final ClickAction getAction(int slot) {
        if (!this.entries.containsKey(slot)) {
            return null;
        }
        return this.entries.get(slot).action;
    }

    public final void setItem(I item, int slot, ClickAction action) {
        Entry ent = new Entry(item, slot, action);
        this.entries.put(slot, ent);
    }

    public final void open(UUID u, int page) {

        if(openGuis.containsKey(u)) {
            openGuis.get(u).close(u);
        }

        openGuis.put(u, this);

        this.players.put(u, page);
        this.onOpened(u, page);
    }

    public final void close(UUID u) {
        if (!this.players.containsKey(u)) {
            return;
        }

        openGuis.remove(u);

        this.players.remove(u);
        this.onClosed(u);
    }

    public static void closeMenu(UUID u) {

        if(!openGuis.containsKey(u)) return;
        openGuis.get(u).close(u);

    }

    public final int getPlayerPage(UUID u) {
        return this.players.get(u);
    }

    public final void onClick(UUID u, ClickType type, int slot) {

        ClickAction act = getAction(slot);

        if(act != null) {
            act.onClick(type, u);
        }
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
        void onClick(ClickType type, UUID user);
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

