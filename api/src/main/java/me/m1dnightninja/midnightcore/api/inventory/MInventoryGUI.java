package me.m1dnightninja.midnightcore.api.inventory;

import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.text.MComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class MInventoryGUI {

    protected final HashMap<Integer, Entry> entries = new HashMap<>();
    protected final HashMap<MPlayer, Integer> players = new HashMap<>();
    protected final MComponent title;
    protected int pageSize = 0;

    private final List<CloseCallback> callbacks = new ArrayList<>();

    protected static final HashMap<MPlayer, MInventoryGUI> openGuis = new HashMap<>();


    protected MInventoryGUI(MComponent title) {
        this.title = title;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageSize() {
        return pageSize == 0 ? 6 : pageSize;
    }

    public int pageCount() {

        int highestEnt = 0;
        for(int i : entries.keySet()) {
            if(i > highestEnt) highestEnt = i;
        }

        return highestEnt / ((pageSize == 0 ? 6 : pageSize) * 9);
    }

    public final void removeItem(int slot) {
        this.entries.remove(slot);
    }

    public final MItemStack getItem(int slot) {
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

    public final void setItem(MItemStack item, int slot, ClickAction action) {
        Entry ent = new Entry(item, slot, action);
        this.entries.put(slot, ent);
    }

    public final void addCallback(CloseCallback cb) {
        callbacks.add(cb);
    }

    public final void open(MPlayer u, int page) {

        if(openGuis.containsKey(u)) {
            openGuis.get(u).close(u);
        }

        openGuis.put(u, this);

        this.players.put(u, page);
        this.onOpened(u, page);
    }

    public final void close(MPlayer u) {
        if (!this.players.containsKey(u)) {
            return;
        }

        openGuis.remove(u);

        this.players.remove(u);
        this.onClosed(u);

        for(CloseCallback cb : callbacks) {
            cb.onClosed(u);
        }
    }

    public static void closeMenu(MPlayer u) {

        if(!openGuis.containsKey(u)) return;
        openGuis.get(u).close(u);

    }

    public final int getPlayerPage(MPlayer u) {
        return this.players.get(u);
    }

    public final void onClick(MPlayer u, ClickType type, int slot) {

        ClickAction act = getAction(slot);

        if(act != null) {
            act.onClick(type, u);
        }
    }

    protected abstract void onClosed(MPlayer u);
    protected abstract void onOpened(MPlayer u, int page);

    protected static class Entry {
        public MItemStack item;
        public int slot;
        public ClickAction action;

        public Entry(MItemStack item, int slot, ClickAction action) {
            this.item = item;
            this.slot = slot;
            this.action = action;
        }
    }

    public interface ClickAction {
        void onClick(ClickType type, MPlayer user);
    }

    public interface CloseCallback {
        void onClosed(MPlayer player);
    }

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
}

