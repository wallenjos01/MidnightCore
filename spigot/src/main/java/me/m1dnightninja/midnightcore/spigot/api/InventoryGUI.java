package me.m1dnightninja.midnightcore.spigot.api;

import me.m1dnightninja.midnightcore.api.AbstractInventoryGUI;
import me.m1dnightninja.midnightcore.spigot.MidnightCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class InventoryGUI extends AbstractInventoryGUI<ItemStack> implements Listener {

    public InventoryGUI(String title) {
        super(title);
    }

    @Override
    protected void onClosed(UUID u) {
        Player p = Bukkit.getPlayer(u);
        if(p != null) {
            p.closeInventory();
        }
        if(players.size() == 0) {
            HandlerList.unregisterAll(this);
        }
    }

    @Override
    protected void onOpened(UUID u, int page) {

        Player p = Bukkit.getPlayer(u);
        if(p == null) return;

        int offset = page * 54;
        if(offset > entries.size()) {
            return;
        }

        int items = Math.min(54, entries.size() - offset);

        Inventory inv = Bukkit.createInventory(null, InventoryType.CHEST, title);
        for(int i = 0; i < items ; i++) {

            ItemStack is = entries.get(offset + i).item;
            inv.setItem(i, is);
        }

        p.openInventory(inv);

        if(players.size() == 1) {
            Bukkit.getPluginManager().registerEvents(this, MidnightCore.getPlugin(MidnightCore.class));
        }

    }

    @EventHandler
    public void onClick(InventoryClickEvent ev) {

        if (!players.containsKey(ev.getWhoClicked().getUniqueId())) return;

        ev.setCancelled(true);

        int offset = players.get(ev.getWhoClicked().getUniqueId()) * 54;
        ClickAction act = entries.get(ev.getSlot() + offset).action;

        if (act == null) return;

        try {
            act.onClick(ClickType.valueOf(ev.getClick().name()));
        } catch (IllegalArgumentException ex) {
            // Ignore
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent ev) {
        if(!players.containsKey(ev.getPlayer().getUniqueId())) return;
        close(ev.getPlayer().getUniqueId());
    }
}
