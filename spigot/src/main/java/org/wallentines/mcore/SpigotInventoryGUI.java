package org.wallentines.mcore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.util.ConversionUtil;

import java.util.HashMap;

public class SpigotInventoryGUI extends InventoryGUI {

    private final HashMap<Player, Inventory> players = new HashMap<>();

    public SpigotInventoryGUI(Component title, int rows) {
        super(title, rows);
    }

    @Override
    public void update() {
        for(Player player : players.keySet()) {
            doUpdate(player);
        }
    }

    @Override
    protected void doOpen(org.wallentines.mcore.Player player) {

        SpigotPlayer spl = ConversionUtil.validate(player);
        spl.getInternal().closeInventory();

        Inventory inv = Bukkit.createInventory(null, size);
        spl.getInternal().openInventory(inv);

        players.put(spl.getInternal(), inv);
        doUpdate(spl.getInternal());
    }

    @Override
    protected void doClose(org.wallentines.mcore.Player player) {
        SpigotPlayer spl = ConversionUtil.validate(player);
        if(players.containsKey(spl.getInternal())) {
            players.remove(spl.getInternal());
            spl.getInternal().closeInventory();
        }
    }

    private void doUpdate(Player player) {

        Inventory menu = players.get(player);
        SpigotPlayer spl = new SpigotPlayer(Server.RUNNING_SERVER.get(), player);

        if(menu == null) {
            doOpen(spl);
            doUpdate(player);
            return;
        }

        for(int i = 0 ; i < items.length ; i++) {

            if(items[i] == null) continue;

            ItemStack is = items[i].getItem(spl);
            if(is == null) {
                continue;
            }

            SpigotItem mis = ConversionUtil.validate(is);
            menu.setItem(i, mis.getInternal());
        }

    }

    private static class GUIListener implements Listener {

        private static ClickType getActionType(org.bukkit.event.inventory.ClickType type) {
            return switch (type) {
                case LEFT -> ClickType.LEFT;
                case SHIFT_LEFT -> ClickType.SHIFT_LEFT;
                case RIGHT -> ClickType.RIGHT;
                case SHIFT_RIGHT -> ClickType.SHIFT_RIGHT;
                case MIDDLE -> ClickType.MIDDLE;
                case NUMBER_KEY -> ClickType.NUMBER_KEY;
                case DOUBLE_CLICK -> ClickType.DOUBLE;
                case DROP -> ClickType.THROW;
                case CONTROL_DROP -> ClickType.THROW_ALL;
                default -> null;
            };
        }


        @EventHandler
        private void onClick(InventoryClickEvent event) {

            Player pl = (Player) event.getWhoClicked();
            if(!OPEN_GUIS.containsKey(pl.getUniqueId())) return;

            event.setCancelled(true);

            int slot = event.getSlot();
            InventoryGUI data = OPEN_GUIS.get(pl.getUniqueId());
            ClickType type = getActionType(event.getClick());
            Server.RUNNING_SERVER.get().submit(() -> {
                data.onClick(slot, new SpigotPlayer(Server.RUNNING_SERVER.get(), pl), type);
            });
        }

        @EventHandler
        private void onClose(InventoryCloseEvent event) {
            Player pl = (Player) event.getPlayer();
            closeMenu(new SpigotPlayer(Server.RUNNING_SERVER.get(), pl));
        }

        @EventHandler
        private void onLeave(PlayerQuitEvent event) {
            Player pl = event.getPlayer();
            closeMenu(new SpigotPlayer(Server.RUNNING_SERVER.get(), pl));
        }
    }

    static {

        Bukkit.getPluginManager().registerEvents(new GUIListener(), MidnightCore.getPlugin(MidnightCore.class));

    }
}
