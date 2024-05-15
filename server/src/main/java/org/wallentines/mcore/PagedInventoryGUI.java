package org.wallentines.mcore;

import org.wallentines.mcore.lang.CustomPlaceholder;
import org.wallentines.mcore.lang.UnresolvedComponent;
import org.wallentines.mcore.text.Component;

import java.util.ArrayList;
import java.util.List;

public class PagedInventoryGUI implements InventoryGUI {

    private final UnresolvedComponent title;
    private final SizeProvider sizeProvider;
    private List<Page> pages = new ArrayList<>();
    private final List<RowProvider> topReserved = new ArrayList<>();
    private final List<RowProvider> bottomReserved = new ArrayList<>();
    private int fullSize = -1;

    public PagedInventoryGUI(Component title, SizeProvider provider) {
        this(title, provider, 0);
    }

    public PagedInventoryGUI(Component title, SizeProvider provider, int initialSize) {
        this.title = UnresolvedComponent.completed(title);
        this.sizeProvider = provider;
        resize(initialSize);
    }

    public PagedInventoryGUI(UnresolvedComponent title, SizeProvider provider) {
        this(title, provider, 0);
    }

    public PagedInventoryGUI(UnresolvedComponent title, SizeProvider provider, int size) {
        this.title = title;
        this.sizeProvider = provider;
        resize(size);
    }

    public void open(Player player, int page) {
        Page p = pages.get(page);
        p.gui.open(player);
    }

    public void close(Player player) {
        SingleInventoryGUI.closeMenu(player);
    }

    @Override
    public void closeAll() {
        for(Page p : pages) {
            p.gui.closeAll();
        }
    }

    public void setItem(int index, ItemStack item) {
        setItem(index, item, (SingleInventoryGUI.ClickEvent) null);
    }

    public void setItem(int index, ItemStack item, SingleInventoryGUI.ClickEvent clickEvent) {
        Page p = updateAndGetPage(index);
        int topOffset = topReserved.size() * 9;
        p.gui.setItem(topOffset + index - p.offset, item, clickEvent);
    }

    public void setItem(int index, ItemStack item, PagedClickEvent clickEvent) {
        Page p = updateAndGetPage(index);
        int topOffset = topReserved.size() * 9;
        p.gui.setItem(topOffset + index - p.offset, item, (player, type) -> {
            clickEvent.execute(player, type, p.index);
        });
    }

    public void setItem(int index, UnresolvedItemStack item) {
        setItem(index, item, (SingleInventoryGUI.ClickEvent) null);
    }

    public void setItem(int index, UnresolvedItemStack item, SingleInventoryGUI.ClickEvent clickEvent) {
        Page p = updateAndGetPage(index);
        int topOffset = topReserved.size() * 9;
        p.gui.setItem(topOffset + index - p.offset, item, clickEvent);
    }

    @Override
    public void clearItem(int index) {
        Page p = getPage(index);
        if(p != null) p.gui.clearItem(index - p.offset);
    }

    @Override
    public int rows() {

        return fullSize / 9;
    }

    public void setItem(int index, UnresolvedItemStack item, PagedClickEvent clickEvent) {
        Page p = updateAndGetPage(index);
        int topOffset = topReserved.size() * 9;
        p.gui.setItem(topOffset + index - p.offset, item, (player, type) -> {
            clickEvent.execute(player, type, p.index);
        });
    }

    public void addTopReservedRow(RowProvider rowProvider) {
        topReserved.add(rowProvider);
    }

    public void addBottomReservedRow(RowProvider rowProvider) {
        bottomReserved.add(rowProvider);
    }

    public int pageCount() {
        return pages.size();
    }


    public int size() {
        return fullSize;
    }

    @Override
    public int firstEmpty() {

        for(Page p : pages) {
            int firstEmpty = p.gui.firstEmpty();
            if(firstEmpty != -1) return firstEmpty;
        }

        return -1;
    }

    @Override
    public int lastItem() {

        for(int i = pages.size() - 1 ; i > 0 ; i--) {
            int lastItem = pages.get(i).gui.lastItem();
            if(lastItem != -1) return lastItem;
        }

        return -1;
    }

    @Override
    public void clear() {
        for(Page p : pages) {
            p.gui.closeAll();
        }
        pages.clear();
        resize(0);
    }

    @Override
    public void update() {
        for(Page p : pages) {
            p.gui.update();
        }
    }

    @Override
    public void open(Player player) {
        open(player, 0);
    }

    /**
     * Reserves space for up to the given number of items by resizing immediately
     * @param max The new maximum item.
     */
    public void resize(int max) {
        if(max > fullSize) {
            updatePages(max);
        }
    }

    private Page getPage(int index) {

        for(Page p : pages) {
            if(p.offset + p.size > index) {
                return p;
            }
        }
        return null;
    }

    private Page updateAndGetPage(int index) {
        if(index > fullSize) {
            updatePages(index);
        }
        return getPage(index);
    }

    private void setupReserved(SingleInventoryGUI gui, int page) {

        int offset = 0;
        for(RowProvider rp : topReserved) {
            gui.clear(offset, offset + 9);
            rp.fillRow(page, Row.fromGUI(gui, page, offset), this);
        }
        offset = gui.size - (bottomReserved.size() * 9);
        for(RowProvider rp : bottomReserved) {
            gui.clear(offset, offset + 9);
            rp.fillRow(page, Row.fromGUI(gui, page, offset), this);
        }
    }

    private UnresolvedComponent getPageTitle(int page) {

        if(title.isComplete()) {
            return title;
        }

        UnresolvedComponent cmp = title.copy();
        cmp.getContext()
                .withValue(CustomPlaceholder.inline("gui_page", page + 1))
                .withValue(CustomPlaceholder.inline("gui_pages", () -> String.valueOf(pageCount())));

        return cmp;
    }

    private Page createEmptyPage(int page, int offset, int size) {

        int contentRows = size / 9;
        int rows = contentRows + (topReserved.size()) + (bottomReserved.size());

        SingleInventoryGUI gui = InventoryGUI.create(getPageTitle(page), rows);

        return new Page(gui, offset, page, size);
    }

    private void updatePages(int lastItem) {

        // Find the new page sizes
        List<Integer> newSizes = new ArrayList<>();
        int offset = 0;
        while(offset <= lastItem) {
            int pageRows = sizeProvider.getRows(offset, lastItem, newSizes.size(), this);
            int pageSize = pageRows * 9;

            newSizes.add(pageSize);
            offset += pageSize;
        }
        fullSize = offset - 1;

        List<Page> newPages = new ArrayList<>();

        // Update old pages
        Page partialPage = null;
        int itemsRemaining = 0;
        offset = 0;

        for(Page p : pages) {

            int topOffset = topReserved.size() * 9;
            int pItems = p.size;
            int rpItems = pItems;

            // Finish partial page
            if(itemsRemaining > 0) {

                int realSize = partialPage.size;
                int copyStart = topOffset + realSize - itemsRemaining;
                int copied = Math.min(itemsRemaining, rpItems);

                itemsRemaining -= copied;

                System.arraycopy(p.gui.items, topOffset, partialPage.gui.items, copyStart, copied);
                partialPage.gui.update();

                if(itemsRemaining == 0) {
                    newPages.add(partialPage);
                    partialPage = null;
                }
            }

            // Copy remaining items
            if(rpItems > 0) {

                int index = newPages.size();
                int newPageSize = newSizes.get(index);

                // If the page is the same, just reinsert it.
                if(p.offset == offset && p.size == newPageSize) {
                    if(index == p.index) {
                        newPages.add(p);
                    } else {
                        newPages.add(p.reindex(index, getPageTitle(index)));
                        setupReserved(p.gui, index);
                    }

                // If not, reconstruct it.
                } else {

                    while(rpItems > 0) {

                        partialPage = createEmptyPage(newPages.size(), offset, newPageSize);
                        int contentSize = partialPage.size;

                        int copied = Math.min(contentSize, rpItems);
                        System.arraycopy(p.gui.items, topOffset, partialPage.gui.items, topOffset, copied);
                        partialPage.gui.update();
                        p.gui.moveViewers(partialPage.gui);

                        itemsRemaining = contentSize - copied;
                        if(itemsRemaining == 0) {
                            newPages.add(partialPage);
                            partialPage = null;
                        }

                        rpItems -= copied;
                    }
                }
            }

            offset += pItems;
        }

        if(partialPage != null) {
            offset += itemsRemaining;
            newPages.add(partialPage);
        }

        // Insert new pages
        for(int i = newPages.size(); i < newSizes.size() ; i++) {
            int size = newSizes.get(i);
            newPages.add(createEmptyPage(i, offset, size));
            offset += size;
        }

        this.pages = newPages;

        for(Page p : pages) {
            setupReserved(p.gui, p.index);
        }

    }

    public interface PagedClickEvent {
        void execute(Player player, SingleInventoryGUI.ClickType type, int page);
    }


    public interface SizeProvider {
        int getRows(int localOffset, int page, int lastItem, PagedInventoryGUI gui);

        static SizeProvider fixed(int size) {
            return new Fixed(size);
        }

        static SizeProvider dynamic(int maxSize) {
            return new Dynamic(maxSize);
        }
    }

    private static class Dynamic implements SizeProvider {
        private final int maxPageSize;
        public Dynamic(int maxPageSize) {
            this.maxPageSize = maxPageSize;
        }
        @Override
        public int getRows(int localOffset, int lastItem, int page, PagedInventoryGUI gui) {

            int slots = lastItem - localOffset + 1;
            int fullRows = slots / 9;
            if(fullRows >= maxPageSize) return maxPageSize;

            int partialRow = slots % 9;
            if(partialRow > 0 || fullRows == 0) fullRows++;

            return fullRows;
        }
    }

    private static class Fixed implements SizeProvider {
        private final int pageSize;
        public Fixed(int pageSize) {
            this.pageSize = pageSize;
        }
        @Override
        public int getRows(int localOffset, int lastItem, int page, PagedInventoryGUI gui) {
            return pageSize;
        }
    }


    private static class Page {
        final SingleInventoryGUI gui;
        final int offset;
        final int index;
        final int size;

        Page(SingleInventoryGUI gui, int offset, int index, int size) {
            this.gui = gui;
            this.offset = offset;
            this.index = index;
            this.size = size;
        }

        Page reindex(int index, UnresolvedComponent title) {
            return new Page(gui.copy(title), offset, index, size);
        }
    }

    public interface Row {
        void setItem(int index, UnresolvedItemStack is, PagedClickEvent event);

        static Row fromGUI(SingleInventoryGUI gui, int page, int offset) {
            return (index, is, event) -> {
                if(index < 0 || index > 8) {
                    throw new IllegalStateException("Attempt to place item outside of row bounds!");
                }

                gui.setItem(offset + index, is, (pl, ct) -> {
                    event.execute(pl, ct, page);
                });
            };
        }
    }

    public interface RowProvider {
        void fillRow(int page, Row row, PagedInventoryGUI gui);


        static RowProvider pageControls(UnresolvedItemStack nextPage, UnresolvedItemStack prevPage) {
            return (page, row, gui) -> {

                if(page > 0) {
                    row.setItem(0, prevPage, ((player, type, p) -> {
                        gui.open(player, p - 1);
                    }));
                }
                if(page < gui.pageCount()) {
                    row.setItem(8, nextPage, ((player, type, p) -> {
                        gui.open(player, p + 1);
                    }));
                }
            };
        }

    }

}
