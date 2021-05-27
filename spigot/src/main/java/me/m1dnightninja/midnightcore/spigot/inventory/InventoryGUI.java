package me.m1dnightninja.midnightcore.spigot.inventory;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.inventory.AbstractInventoryGUI;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.spigot.MidnightCore;
import me.m1dnightninja.midnightcore.spigot.player.SpigotPlayer;
import me.m1dnightninja.midnightcore.spigot.util.ConversionUtil;
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

public class InventoryGUI extends AbstractInventoryGUI implements Listener {

    public InventoryGUI(MComponent title) {
        super(title);
    }

    @Override
    protected void onClosed(MPlayer u) {
        Player p = ((SpigotPlayer) u).getSpigotPlayer();

        if (p != null) {
            p.closeInventory();
        }
        if (this.players.size() == 0) {
            HandlerList.unregisterAll(this);
        }
    }

    @Override
    protected void onOpened(MPlayer u, int page) {
        Player p = ((SpigotPlayer) u).getSpigotPlayer();
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
        MPlayer pl = MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(ev.getWhoClicked().getUniqueId());
        if (!this.players.containsKey(pl)) {
            return;
        }
        ev.setCancelled(true);
        int offset = this.players.get(pl) * 54;
        ClickAction act = (this.entries.get((ev.getSlot() + offset))).action;
        if (act == null) {
            return;
        }
        try {
            act.onClick(ClickType.valueOf(ev.getClick().name()), pl);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // Ignore
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent ev) {
        MPlayer pl = MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(ev.getPlayer().getUniqueId());
        if (!this.players.containsKey(pl)) {
            return;
        }
        this.close(pl);
    }
}

