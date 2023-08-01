package org.wallentines.mcore.item;

import org.wallentines.mcore.Player;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ComponentResolver;

import java.util.List;

public class UnresolvedItemStack {

    private final ItemStack.Builder builder;
    private final Component title;
    private final List<Component> lore;

    public UnresolvedItemStack(ItemStack.Builder builder, Component name, List<Component> lore) {
        this.builder = builder;
        this.title = name;
        this.lore = lore == null ? List.of() : List.copyOf(lore);
    }

    public ItemStack resolve(Player player) {

        ItemStack.Builder out = builder.copy();
        if(title != null) {
            out.withName(ComponentResolver.resolveComponent(title, player));
        }
        if(lore != null && !lore.isEmpty()) {
            out.withLore(lore.stream().map(cmp -> ComponentResolver.resolveComponent(cmp,player)).toList());
        }

        return out.build();
    }


}
