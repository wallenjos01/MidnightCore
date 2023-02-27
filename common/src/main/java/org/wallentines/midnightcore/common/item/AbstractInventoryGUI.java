package org.wallentines.midnightcore.common.item;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
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
    protected final List<PageData> pages = new ArrayList<>();
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
        this.pages.clear();
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

        return (int) Math.ceil((float) highestEnt / (float) ((pageSize == 0 ? 6 : pageSize) * 9));
    }

    @Override
    public final void removeItem(int slot) {
        this.entries.remove(slot);
        this.pages.clear();
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
        this.pages.clear();
    }

    @Override
    public final void addCloseCallback(Consumer<MPlayer> cb) {
        callbacks.add(cb);
    }

    @Override
    public final void open(MPlayer u, int page) {

        if(openGuis.containsKey(u) && openGuis.get(u) != this) {
            openGuis.get(u).close(u);
        }

        if(pages.isEmpty()) {
            updatePages();
        }

        this.onOpened(u, page);
        this.onUpdate(u, page);

        this.players.put(u, page);
        openGuis.put(u, this);
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
    protected abstract void onUpdate(MPlayer u, int page);

    @Override
    public void update() {

        for(MPlayer player : players.keySet()) {
            update(player);
        }
    }

    @Override
    public void update(MPlayer u) {

        if(this.pages.isEmpty()) updatePages();

        int page = getPage(u);
        if(page == -1) {
            MidnightCoreAPI.getLogger().warn("Attempt to update a menu for a player who doesn't have the menu open!");
            return;
        }
        if(page >= pages.size()) {
            if(page > 0) open(u, 0);
            return;
        }

        onUpdate(u, page);
    }

    @Override
    public final int getPage(MPlayer u) {
        return this.players.getOrDefault(u, -1);
    }

    @Override
    public int getPageOffset(int page) {

        if(pages.isEmpty()) updatePages();
        if(pages.size() <= page || page < 0) return -1;

        return pages.get(page).offset;
    }

    public final void onClick(MPlayer u, ClickType type, int slot) {

        if(this.pages.isEmpty()) updatePages();
        GUIAction act = getAction(slot);

        if(act != null) {
            act.onClick(type, u);
        }
    }

    public static void closeMenu(MPlayer u) {

        if(!openGuis.containsKey(u)) return;
        openGuis.get(u).close(u);
    }

    private void updatePages() {

        pages.clear();
        for(int i = 0 ; i < pageCount() ; i++) {
            pages.add(findPageData(i));
        }
    }

    protected PageData getPageData(int page) {

        if(pages.isEmpty()) {
            updatePages();
        }

        if(page > pages.size()) return null;
        return pages.get(page);
    }

    private PageData findPageData(int page) {

        int size;
        int offset;

        if(pageSize == 0) {

            offset = page * 54;
            int max = 0;

            for (Entry ent : entries.values()) {
                if (ent.slot > max && ent.slot < offset + 54) {
                    max = ent.slot;
                    if(ent.slot == offset + 53) break;
                }
            }

            size = ((max - offset) / 9) + 1;

        } else {

            size = pageSize;
            offset = page * (pageSize * 9);
        }

        return new PageData(size, offset);
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

    protected static class PageData {

        public int size;
        public int offset;

        public PageData(int size, int offset) {
            this.size = size;
            this.offset = offset;
        }
    }

}
