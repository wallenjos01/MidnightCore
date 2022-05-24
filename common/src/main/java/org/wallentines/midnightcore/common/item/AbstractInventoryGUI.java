package org.wallentines.midnightcore.common.item;

import org.wallentines.midnightcore.api.item.InventoryGUI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractInventoryGUI implements InventoryGUI {

    protected final HashMap<Integer, Entry> entries = new HashMap<>();
    protected final HashMap<MPlayer, Integer> players = new HashMap<>();
    protected final MComponent title;
    protected int pageSize = 0;

    private final List<Consumer<MPlayer>> callbacks = new ArrayList<>();

    protected static final HashMap<MPlayer, AbstractInventoryGUI> openGuis = new HashMap<>();

    protected AbstractInventoryGUI(MComponent title) {
        this.title = title;
    }

    @Override
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public int getPageSize() {
        return pageSize == 0 ? 6 : pageSize;
    }

    @Override
    public int pageCount() {

        int highestEnt = 0;
        for(int i : entries.keySet()) {
            if(i > highestEnt) highestEnt = i;
        }

        return highestEnt / ((pageSize == 0 ? 6 : pageSize) * 9);
    }

    @Override
    public final void removeItem(int slot) {
        this.entries.remove(slot);
    }

    @Override
    public final MItemStack getItem(int slot) {
        if (!this.entries.containsKey(slot)) {
            return null;
        }
        return this.entries.get(slot).item;
    }

    @Override
    public final GUIAction getAction(int slot) {
        if (!this.entries.containsKey(slot)) {
            return null;
        }
        return this.entries.get(slot).action;
    }

    @Override
    public final void setItem(int slot, MItemStack item, GUIAction action) {
        Entry ent = new Entry(item, slot, action);
        this.entries.put(slot, ent);
    }

    @Override
    public final void addCloseCallback(Consumer<MPlayer> cb) {
        callbacks.add(cb);
    }

    @Override
    public final void open(MPlayer u, int page) {

        if(openGuis.containsKey(u)) {
            openGuis.get(u).close(u);
        }

        openGuis.put(u, this);

        this.players.put(u, page);
        this.onOpened(u, page);
    }

    @Override
    public final void close(MPlayer u) {
        if (!this.players.containsKey(u)) {
            return;
        }

        openGuis.remove(u);

        this.players.remove(u);
        this.onClosed(u);

        for(Consumer<MPlayer> cb : callbacks) {
            cb.accept(u);
        }
    }

    protected abstract void onClosed(MPlayer u);
    protected abstract void onOpened(MPlayer u, int page);

    @Override
    public final int getPage(MPlayer u) {
        return this.players.get(u);
    }

    public final void onClick(MPlayer u, ClickType type, int slot) {

        GUIAction act = getAction(slot);

        if(act != null) {
            act.onClick(type, u);
        }
    }

    public static void closeMenu(MPlayer u) {

        if(!openGuis.containsKey(u)) return;
        openGuis.get(u).close(u);
    }

    protected static class Entry {
        public MItemStack item;
        public int slot;
        public GUIAction action;

        public Entry(MItemStack item, int slot, GUIAction action) {
            this.item = item;
            this.slot = slot;
            this.action = action;
        }
    }


}
