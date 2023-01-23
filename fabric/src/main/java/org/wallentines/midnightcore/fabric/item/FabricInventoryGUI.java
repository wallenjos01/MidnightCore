package org.wallentines.midnightcore.fabric.item;

import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.common.item.AbstractInventoryGUI;
import org.wallentines.midnightcore.fabric.event.player.ContainerClickEvent;
import org.wallentines.midnightcore.fabric.event.player.ContainerCloseEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerLeaveEvent;
import org.wallentines.midnightcore.fabric.mixin.AccessorServerPlayer;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightlib.event.Event;


public class FabricInventoryGUI extends AbstractInventoryGUI {

    public FabricInventoryGUI(MComponent title) {
        super(title);
    }

    @Override
    protected void onClosed(MPlayer u) {
        ServerPlayer pl = ((FabricPlayer) u).getInternal();
        if(pl != null && pl.containerMenu != pl.inventoryMenu) {
            pl.closeContainer();
        }
    }

    @Override
    protected void onOpened(MPlayer u, int page) {

        ServerPlayer player = ((FabricPlayer) u).getInternal();
        if(player == null || player.hasDisconnected()) return;

        if(player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
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

        SimpleContainer inv = new SimpleContainer(rows * 9);

        for(Entry ent : entries.values()) {

            if(ent.slot < offset || ent.slot >= (offset + (rows * 9)) || ent.item == null) {
                continue;
            }

            ItemStack is = ((FabricItem) ent.item).getInternal();

            inv.setItem(ent.slot - offset, is);
        }

        ChestMenu handler = createScreen(rows, player, inv);

        player.connection.send(new ClientboundOpenScreenPacket(handler.containerId, handler.getType(), ConversionUtil.toComponent(title)));
        player.containerMenu = handler;

        ((AccessorServerPlayer) player).callInitMenu(handler);
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

    private static void onClick(ContainerClickEvent event) {

        FabricPlayer pl = FabricPlayer.wrap(event.getPlayer());
        MinecraftServer server = event.getPlayer().getServer();
        if(server == null) return;

        AbstractInventoryGUI gui = openGuis.get(pl);
        if(gui == null) {
            return;
        }

        event.setCancelled(true);

        int offset = gui.getPage(pl) * (gui.getPageSize() * 9);
        int slot = event.getSlot();

        server.submit(() -> gui.onClick(pl, getActionType(event.getClickType(), event.getAction()), offset + slot));
    }

    private static void onClose(ContainerCloseEvent event) {

        MPlayer player = FabricPlayer.wrap(event.getPlayer());

        if(player != null && event.getPlayer().containerMenu != event.getPlayer().inventoryMenu) {
            closeMenu(player);
        }
    }

    private static void onLeave(PlayerLeaveEvent event) {
        closeMenu(FabricPlayer.wrap(event.getPlayer()));
    }

    public static void registerEvents(Object owner) {

        Event.register(ContainerClickEvent.class, owner, FabricInventoryGUI::onClick);
        Event.register(ContainerCloseEvent.class, owner, FabricInventoryGUI::onClose);
        Event.register(PlayerLeaveEvent.class, owner, FabricInventoryGUI::onLeave);

    }

    static {

        registerEvents(FabricInventoryGUI.class);

    }

}
