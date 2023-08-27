package org.wallentines.mcore;

import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.wallentines.fbev.player.ContainerClickEvent;
import org.wallentines.fbev.player.ContainerCloseEvent;
import org.wallentines.fbev.player.PlayerLeaveEvent;
import org.wallentines.mcore.mixin.AccessorServerPlayer;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.WrappedComponent;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.midnightlib.event.Event;

import java.util.HashMap;

public class FabricInventoryGUI extends InventoryGUI {

    private final HashMap<WrappedPlayer, ChestMenu> players = new HashMap<>();

    public FabricInventoryGUI(Component title, int rows) {
        super(title, rows);

        Event.register(ContainerClickEvent.class, this, ev -> {

            Player pl = ev.getPlayer();
            if(!players.containsKey(pl.wrap())) return;
            ev.setCancelled(true);

            int slot = ev.getSlot();

            ev.getPlayer().server.submit(() -> onClick(slot, pl, getActionType(ev.getClickType(), ev.getAction())));
        });

        Event.register(ContainerCloseEvent.class, this, ev -> {
            Player pl = ev.getPlayer();
            if(!players.containsKey(pl.wrap())) return;
            close(pl);
        });

        Event.register(PlayerLeaveEvent.class, this, ev -> {
            Player pl = ev.getPlayer();
            if(!players.containsKey(pl.wrap())) return;
            close(pl);
        });

    }

    @Override
    public void update() {

        for(WrappedPlayer wpl : players.keySet()) {
            doUpdate(ConversionUtil.validate(wpl.get()));
        }

    }

    @Override
    protected void doOpen(Player player) {

        ServerPlayer spl = ConversionUtil.validate(player);
        if(spl.hasDisconnected()) return;

        if(spl.containerMenu != spl.inventoryMenu) {
            spl.closeContainer();
        }

        SimpleContainer inv = new SimpleContainer(size);
        ChestMenu handler = createScreen(size / 9, spl, inv);

        spl.connection.send(new ClientboundOpenScreenPacket(handler.containerId, handler.getType(), WrappedComponent.resolved(title, spl)));
        spl.containerMenu = handler;

        players.put(player.wrap(), handler);
        doUpdate(spl);

        ((AccessorServerPlayer) player).callInitMenu(handler);
    }

    @Override
    protected void doClose(Player player) {

        ServerPlayer pl = ConversionUtil.validate(player);
        players.remove(player.wrap());
        if(pl != null && pl.containerMenu != pl.inventoryMenu) {
            pl.closeContainer();
        }

    }

    private void doUpdate(ServerPlayer player) {

        ChestMenu menu = players.get(player.wrap());
        if(menu == null) {
            doOpen(player);
            doUpdate(player);
            return;
        }

        for(int i = 0 ; i < items.length ; i++) {

            if(items[i] == null) continue;

            org.wallentines.mcore.ItemStack is = items[i].getItem(player);
            if(is == null) {
                continue;
            }

            ItemStack mis = ConversionUtil.validate(is);
            menu.getContainer().setItem(i, mis);
        }
    }

    private static ClickType getActionType(int action, net.minecraft.world.inventory.ClickType type) {
        return switch (type) {
            case PICKUP -> action == 0 ? ClickType.LEFT : ClickType.RIGHT;
            case QUICK_MOVE -> action == 0 ? ClickType.SHIFT_LEFT : ClickType.SHIFT_RIGHT;
            case SWAP -> ClickType.NUMBER_KEY;
            case CLONE -> ClickType.MIDDLE;
            case THROW -> action == 0 ? ClickType.THROW : ClickType.THROW_ALL;
            case PICKUP_ALL -> ClickType.DOUBLE;
            default -> null;
        };
    }

    private ChestMenu createScreen(int rows, ServerPlayer player, Container inv) {

        ((AccessorServerPlayer) player).callNextContainerCounter();
        int syncId = ((AccessorServerPlayer) player).getContainerCounter();

        return switch (rows) {
            case 1 -> new ChestMenu(MenuType.GENERIC_9x1, syncId, player.getInventory(), inv, rows);
            case 2 -> new ChestMenu(MenuType.GENERIC_9x2, syncId, player.getInventory(), inv, rows);
            case 3 -> new ChestMenu(MenuType.GENERIC_9x3, syncId, player.getInventory(), inv, rows);
            case 4 -> new ChestMenu(MenuType.GENERIC_9x4, syncId, player.getInventory(), inv, rows);
            case 5 -> new ChestMenu(MenuType.GENERIC_9x5, syncId, player.getInventory(), inv, rows);
            default -> new ChestMenu(MenuType.GENERIC_9x6, syncId, player.getInventory(), inv, rows);
        };
    }
}
