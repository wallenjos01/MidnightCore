package org.wallentines.mcore;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.WrappedComponent;
import org.wallentines.mcore.util.ConversionUtil;

import java.util.ArrayList;
import java.util.List;

public class FabricInventoryGUI extends InventoryGUI {

    private final List<Menu> open = new ArrayList<>();

    public FabricInventoryGUI(Component title, int rows) {
        super(title, rows);
    }

    @Override
    public void update() {

        open.removeIf(m -> m.player.get() == null);
        for(Menu m : open) {
            m.update();
        }
    }

    @Override
    protected void doOpen(Player player) {

        ServerPlayer spl = ConversionUtil.validate(player);
        if(spl.hasDisconnected()) return;

        if(spl.containerMenu != spl.inventoryMenu) {
            spl.closeContainer();
        }
        spl.openMenu(new MenuProvider() {
            @Override
            public @NotNull net.minecraft.network.chat.Component getDisplayName() {
                return WrappedComponent.resolved(FabricInventoryGUI.this.title, spl);
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, net.minecraft.world.entity.player.Player player) {

                if(!(player instanceof ServerPlayer spl)) return null;
                Menu out = new Menu(id, spl);
                open.add(out);

                return out;
            }
        });
    }

    @Override
    protected void doClose(Player player) {

        ServerPlayer pl = ConversionUtil.validate(player);
        if(pl != null && pl.containerMenu != pl.inventoryMenu) {
            pl.closeContainer();
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

    private class Menu extends AbstractContainerMenu {

        private final WrappedPlayer player;

        Menu(int id, ServerPlayer spl) {
            super(getMenuType(FabricInventoryGUI.this.size / 9), id);

            this.player = new WrappedPlayer(spl);

            Container container = new SimpleContainer(FabricInventoryGUI.this.size);
            for(int i = 0 ; i < FabricInventoryGUI.this.size ; i++) {
                int row = i / 9;
                int col = i % 9;
                addSlot(new Slot(container, i, row, col));
            }

            update();
        }

        public void update() {

            Player spl = player.get();
            if(spl == null || !spl.isOnline()) {
                return;
            }

            int stateId = incrementStateId();

            for(int i = 0 ; i < FabricInventoryGUI.this.size ; i++) {
                Entry ent = FabricInventoryGUI.this.items[i];
                if(ent == null) continue;

                org.wallentines.mcore.ItemStack is = ent.getItem(spl);
                if(is != null) {
                    setItem(i, stateId, ConversionUtil.validate(is));
                }
            }
        }

        @Override
        public @NotNull ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int i) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(net.minecraft.world.entity.player.Player player) {
            return true;
        }

        @Override
        public void clicked(int slot, int button, net.minecraft.world.inventory.ClickType clickType, net.minecraft.world.entity.player.Player player) {

            if(player.level().isClientSide || slot < 0 || slot >= items.length) return;
            FabricInventoryGUI.this.onClick(slot, (ServerPlayer) player, getActionType(button, clickType));
        }

        private static MenuType<?> getMenuType(int rows) {
            return switch (rows) {
                case 1 -> MenuType.GENERIC_9x1;
                case 2 -> MenuType.GENERIC_9x2;
                case 3 -> MenuType.GENERIC_9x3;
                case 4 -> MenuType.GENERIC_9x4;
                case 5 -> MenuType.GENERIC_9x5;
                default -> MenuType.GENERIC_9x6;
            };
        }

    }
}
