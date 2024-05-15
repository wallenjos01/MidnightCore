package org.wallentines.mcore;

import org.wallentines.mcore.lang.PlaceholderContext;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.lang.UnresolvedComponent;
import org.wallentines.mcore.text.Component;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UnresolvedItemStack {

    private final ItemStack.Builder builder;
    private final PlaceholderManager manager;
    private final UnresolvedComponent title;
    private final List<UnresolvedComponent> lore;

    public UnresolvedItemStack(ItemStack.Builder builder, Component name) {
        this(builder, UnresolvedComponent.completed(name), List.of(), PlaceholderManager.INSTANCE);
    }

    public UnresolvedItemStack(ItemStack.Builder builder, UnresolvedComponent name, List<UnresolvedComponent> lore) {
        this(builder, name, lore, PlaceholderManager.INSTANCE);
    }

    public UnresolvedItemStack(ItemStack.Builder builder, UnresolvedComponent name, List<UnresolvedComponent> lore, PlaceholderManager manager) {
        this.builder = builder;
        this.title = name;
        this.lore = lore == null ? List.of() : List.copyOf(lore);
        this.manager = manager;
    }

    public ItemStack resolve(Player player) {

        ItemStack.Builder out = builder.copy();
        PlaceholderContext ctx = new PlaceholderContext();
        ctx.addValue(player);

        if(title != null) {
            out.withName(title.resolve(manager, ctx));
        }
        if(lore != null && !lore.isEmpty()) {
            out.withLore(lore.stream().map(cmp -> cmp.resolve(manager, ctx)).toList());
        }

        return out.build();
    }


    public static final Serializer<UnresolvedItemStack> SERIALIZER = new Serializer<UnresolvedItemStack>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, UnresolvedItemStack value) {

            SerializeResult<O> out = ItemStack.Builder.SERIALIZER.serialize(context, value.builder, GameVersion.CURRENT_VERSION.get());
            if(!out.isComplete()) return out;

            O map = out.getOrThrow();
            if(value.title != null) {
                SerializeResult<O> title = UnresolvedComponent.SERIALIZER.serialize(context, value.title);
                if(!title.isComplete()) return title;
                context.set("name", title.getOrThrow(), map);
            }
            if(value.lore != null) {
                SerializeResult<O> lore = UnresolvedComponent.SERIALIZER.listOf().serialize(context, value.lore);
                if(!lore.isComplete()) return lore;
                context.set("lore", lore.getOrThrow(), map);
            }

            return SerializeResult.success(map);
        }

        @Override
        public <O> SerializeResult<UnresolvedItemStack> deserialize(SerializeContext<O> context, O value) {

            return ItemStack.Builder.SERIALIZER.deserialize(context, value, GameVersion.CURRENT_VERSION.get()).map(builder -> {

                UnresolvedComponent name = null;
                List<UnresolvedComponent> lore = null;

                O encodedName = context.get("name", value);
                if(encodedName != null) {
                    SerializeResult<UnresolvedComponent> res = UnresolvedComponent.SERIALIZER.deserialize(context, encodedName);
                    if(!res.isComplete()) {
                        return SerializeResult.failure(res.getError());
                    }
                    name = res.getOrThrow();
                }

                O encodedLore = context.get("lore", value);
                if(encodedLore != null) {
                    SerializeResult<Collection<UnresolvedComponent>> res = UnresolvedComponent.SERIALIZER.listOf().deserialize(context, encodedLore);
                    if(!res.isComplete()) {
                        return SerializeResult.failure(res.getError());
                    }
                    lore = new ArrayList<>(res.getOrThrow());
                }

                return SerializeResult.success(new UnresolvedItemStack(builder, name, lore));
            });
        }
    };


}
