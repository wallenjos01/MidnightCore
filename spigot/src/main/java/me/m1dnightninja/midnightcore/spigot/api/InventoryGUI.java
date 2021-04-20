package me.m1dnightninja.midnightcore.spigot.api;

import java.util.UUID;
import me.m1dnightninja.midnightcore.api.AbstractInventoryGUI;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.spigot.MidnightCore;
import me.m1dnightninja.midnightcore.spigot.util.ConversionUtil;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Conversion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryGUI extends AbstractInventoryGUI implements Listener {

    public InventoryGUI(MComponent title) {
        super(title);
    }

    @Override
    protected void onClosed(UUID u) {
        Player p = Bukkit.getPlayer(u);
        if (p != null) {
            p.closeInventory();
        }
        if (this.players.size() == 0) {
            HandlerList.unregisterAll(this);
        }
    }

    @Override
    protected void onOpened(UUID u, int page) {
        Player p = Bukkit.getPlayer(u);
        if (p == null) {
            return;
        }
        int offset = page * 54;
        if (offset > this.entries.size()) {
            return;
        }
        int items = Math.min(54, this.entries.size() - offset);
        Inventory inv = Bukkit.createInventory(null, InventoryType.CHEST, this.title.toLegacyText(false));
        for (int i = 0; i < items; ++i) {
            ItemStack is = ConversionUtil.toBukkitStack((this.entries.get((offset + i))).item);
            inv.setItem(i, is);
        }
        p.openInventory(inv);
        if (this.players.size() == 1) {
            Bukkit.getPluginManager().registerEvents(this, MidnightCore.getPlugin(MidnightCore.class));
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent ev) {
        if (!this.players.containsKey(ev.getWhoClicked().getUniqueId())) {
            return;
        }
        ev.setCancelled(true);
        int offset = this.players.get(ev.getWhoClicked().getUniqueId()) * 54;
        ClickAction act = (this.entries.get((ev.getSlot() + offset))).action;
        if (act == null) {
            return;
        }
        try {
            act.onClick(ClickType.valueOf(ev.getClick().name()), ev.getWhoClicked().getUniqueId());
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // Ignore
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent ev) {
        if (!this.players.containsKey(ev.getPlayer().getUniqueId())) {
            return;
        }
        this.close(ev.getPlayer().getUniqueId());
    }
}

