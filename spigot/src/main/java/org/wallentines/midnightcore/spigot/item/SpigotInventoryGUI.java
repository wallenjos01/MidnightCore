package org.wallentines.midnightcore.spigot.item;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.common.item.AbstractInventoryGUI;
import org.wallentines.midnightcore.spigot.MidnightCore;
import org.wallentines.midnightcore.spigot.player.SpigotPlayer;

import java.util.HashMap;

public class SpigotInventoryGUI extends AbstractInventoryGUI {


    private final HashMap<MPlayer, Inventory> playerMenus = new HashMap<>();

    public SpigotInventoryGUI(MComponent title) {
        super(title);
    }

    @Override
    protected void onClosed(MPlayer u) {

        Player pl = ((SpigotPlayer) u).getInternal();
        if(pl == null || !pl.isOnline()) return;

        pl.closeInventory();
    }

    @Override
    protected void onOpened(MPlayer u, int page) {

        Player pl = ((SpigotPlayer) u).getInternal();
        if(pl == null || !pl.isOnline()) return;

        pl.closeInventory();

        PageData pageData = getPageData(page);

        Inventory inv = Bukkit.createInventory(null, pageData.size * 9, title.toLegacyText());
        playerMenus.put(u, inv);

        onUpdate(u, page);
        pl.openInventory(inv);
    }

    @Override
    public void onUpdate(MPlayer u, int page) {

        Inventory inv = playerMenus.get(u);

        if(inv == null) {
            if(page > 0) onOpened(u, 0);
            return;
        }

        PageData pageData = getPageData(page);
        for(Entry ent : entries.values()) {

            if(ent.slot < pageData.offset || ent.slot >= (pageData.offset + (pageData.size * 9)) || ent.item == null) {
                continue;
            }

            ItemStack is = ItemHelper.getInternal(ent.item);
            inv.setItem(ent.slot - pageData.offset, is);
        }
    }

    private static class GUIListener implements Listener {

        private static ClickType getActionType(org.bukkit.event.inventory.ClickType type) {
            switch (type) {
                case LEFT: return ClickType.LEFT;
                case SHIFT_LEFT: return ClickType.SHIFT_LEFT;
                case RIGHT: return ClickType.RIGHT;
                case SHIFT_RIGHT: return ClickType.SHIFT_RIGHT;
                case MIDDLE: return ClickType.MIDDLE;
                case NUMBER_KEY: return ClickType.NUMBER_KEY;
                case DOUBLE_CLICK: return ClickType.DOUBLE;
                case DROP: return ClickType.THROW;
                case CONTROL_DROP: return ClickType.THROW_ALL;
                default: return null;
            }
        }


        @EventHandler
        private void onClick(InventoryClickEvent event) {

            Player pl = (Player) event.getWhoClicked();
            MPlayer mpl = SpigotPlayer.wrap(pl);

            AbstractInventoryGUI gui = openGuis.get(mpl);
            if(gui == null) {
                return;
            }

            event.setCancelled(true);

            int offset = gui.getPage(mpl) * (gui.getPageSize() * 9);
            int slot = event.getSlot();

            gui.onClick(mpl, getActionType(event.getClick()), offset + slot);
        }

        @EventHandler
        private void onClose(InventoryCloseEvent event) {
            closeMenu(SpigotPlayer.wrap((Player) event.getPlayer()));
        }

        @EventHandler
        private void onLeave(PlayerQuitEvent event) {
            closeMenu(SpigotPlayer.wrap(event.getPlayer()));
        }
    }

    static {

        Bukkit.getServer().getPluginManager().registerEvents(new GUIListener(), MidnightCore.getInstance());

    }
}
