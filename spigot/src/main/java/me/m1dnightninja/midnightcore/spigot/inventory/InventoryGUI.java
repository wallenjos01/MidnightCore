package me.m1dnightninja.midnightcore.spigot.inventory;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.inventory.AbstractInventoryGUI;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.spigot.MidnightCore;
import me.m1dnightninja.midnightcore.spigot.player.SpigotPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

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

        Inventory inv = Bukkit.createInventory(null, rows * 9, this.title.toLegacyText(false));
        for(Entry ent : entries.values()) {

            if(ent.slot < offset || ent.slot >= (offset + (rows * 9)) || ent.item == null) {
                continue;
            }

            inv.setItem(ent.slot - offset, ((SpigotItem) (ent.item)).getBukkitStack());
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

        int offset = openGuis.get(pl).getPlayerPage(pl) * 54;
        int slot = ev.getSlot();

        AbstractInventoryGUI.Entry ent = (this.entries.get((offset + slot)));
        if (ent == null || ent.action == null) {
            return;
        }
        try {
            ent.action.onClick(ClickType.valueOf(ev.getClick().name()), pl);
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

