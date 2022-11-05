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
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.common.item.AbstractInventoryGUI;
import org.wallentines.midnightcore.spigot.MidnightCore;
import org.wallentines.midnightcore.spigot.player.SpigotPlayer;

public class SpigotInventoryGUI extends AbstractInventoryGUI {

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

        int offset;
        int rows;

        if(pageSize == 0) {

            int max = 0;
            offset = 54 * page;

            for (Entry ent : entries.values()) {
                if (ent.slot > max) {
                    max = ent.slot;
                }
                if (max > offset + 53) {
                    max = offset + 53;
                    break;
                }
            }

            if (offset > max) {
                return;
            }

            rows = ((max - offset) / 9) + 1;

        } else {

            offset = page * (pageSize * 9);
            rows = pageSize;
        }

        Inventory inv = Bukkit.createInventory(null, rows * 9, title.toLegacyText());

        for(Entry ent : entries.values()) {

            if(ent.slot < offset || ent.slot >= (offset + (rows * 9)) || ent.item == null) {
                continue;
            }

            ItemStack is = ItemConverters.getInternal(ent.item);
            inv.setItem(ent.slot - offset, is);
        }

        pl.openInventory(inv);
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
            closeMenu(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId()));
        }
    }

    static {

        Bukkit.getServer().getPluginManager().registerEvents(new GUIListener(), MidnightCore.getInstance());

    }
}
