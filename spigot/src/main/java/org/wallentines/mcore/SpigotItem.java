package org.wallentines.mcore;

import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.stream.Stream;

public class SpigotItem implements ItemStack {

    private final org.bukkit.inventory.ItemStack internal;
    private final Identifier id;

    public SpigotItem(org.bukkit.inventory.ItemStack internal) {
        this.internal = Adapter.INSTANCE.get().setupInternal(internal);
        this.id = getItemId(this.internal);
    }

    private static Identifier getItemId(org.bukkit.inventory.ItemStack internal) {

        if(internal == null || internal.getType() == Material.AIR) {
            return new Identifier("minecraft", "air");
        }
        return Adapter.INSTANCE.get().getItemId(internal);
    }

    @Override
    public Identifier getType() {
        return id;
    }

    @Override
    public int getCount() {
        return internal.getAmount();
    }

    @Override
    @SuppressWarnings("deprecation")
    public byte getLegacyDataValue() {
        return (byte) internal.getDurability();
    }

    @Override
    public void setCount(int count) {
        internal.setAmount(count);
    }

    @Override
    public void loadComponent(Identifier id, ConfigObject config) {
        if(!getVersion().hasFeature(GameVersion.Feature.ITEM_COMPONENTS)) {
            throw new IllegalStateException("Attempt to load item component on pre-1.20.5!");
        }
        Adapter.INSTANCE.get().loadComponent(internal, id, config);
    }

    @Override
    public @Nullable ConfigObject saveComponent(Identifier id) {
        if(!getVersion().hasFeature(GameVersion.Feature.ITEM_COMPONENTS)) {
            throw new IllegalStateException("Attempt to save item component on pre-1.20.5!");
        }
        return Adapter.INSTANCE.get().saveComponent(internal, id);
    }

    @Override
    public void removeComponent(Identifier id) {

        if(!getVersion().hasFeature(GameVersion.Feature.ITEM_COMPONENTS)) {
            throw new IllegalStateException("Attempt to remove item component on pre-1.20.5!");
        }
        Adapter.INSTANCE.get().removeComponent(internal, id);
    }

    @Override
    public Stream<Identifier> getComponentIds() {
        return Stream.empty();
    }

    @Override
    public @Nullable ConfigSection getCustomData() {
        if(getVersion().hasFeature(GameVersion.Feature.ITEM_COMPONENTS)) {
            ConfigObject obj = saveComponent(CUSTOM_DATA_COMPONENT);
            if(obj == null || !obj.isSection()) return null;
            return obj.asSection();
        }
        return Adapter.INSTANCE.get().getTag(internal);
    }

    @Override
    public void setCustomData(ConfigSection section) {
        if(getVersion().hasFeature(GameVersion.Feature.ITEM_COMPONENTS)) {
            loadComponent(CUSTOM_DATA_COMPONENT, section);
            return;
        }
        Adapter.INSTANCE.get().setTag(internal, section);
    }

    @Override
    public void grow(int amount) {
        setCount(internal.getAmount() + amount);
    }

    @Override
    public void shrink(int amount) {
        setCount(internal.getAmount() - amount);
    }

    @Override
    public String getTranslationKey() {
        return Adapter.INSTANCE.get().getTranslationKey(internal);
    }

    @Override
    public GameVersion getVersion() {
        return Adapter.INSTANCE.get().getGameVersion();
    }

    @Override
    public Color getRarityColor() {
        return Adapter.INSTANCE.get().getRarityColor(internal);
    }

    @Override
    public ItemStack copy() {
        return new SpigotItem(internal.clone());
    }

    public org.bukkit.inventory.ItemStack getInternal() {
        return internal;
    }

    public static class Factory implements ItemStack.Factory {

        @Override
        public ItemStack buildLegacy(Identifier id, int count, byte damage, ConfigSection tag, GameVersion version) {

            if(Server.RUNNING_SERVER.get().getVersion().hasFeature(GameVersion.Feature.NAMESPACED_IDS)) {
                throw new IllegalStateException("Cannot create legacy item on this version!");
            }

            org.bukkit.inventory.ItemStack bis = Adapter.INSTANCE.get().buildItem(id, count, damage);
            ItemStack out = new SpigotItem(bis);
            if(tag != null) out.setCustomData(tag);

            return out;
        }

        @Override
        public ItemStack buildTagged(Identifier id, int count, ConfigSection tag, GameVersion version) {

            GameVersion ver = Server.RUNNING_SERVER.get().getVersion();
            if(ver.hasFeature(GameVersion.Feature.ITEM_COMPONENTS) || !ver.hasFeature(GameVersion.Feature.NAMESPACED_IDS)) {
                throw new IllegalStateException("Cannot create tagged item on this version!");
            }

            org.bukkit.inventory.ItemStack bis = Adapter.INSTANCE.get().buildItem(id, count, (byte) 0);
            ItemStack out = new SpigotItem(bis);
            if(tag != null) out.setCustomData(tag);

            return out;
        }

        @Override
        public ItemStack buildStructured(Identifier id, int count, ComponentPatchSet components, GameVersion version) {

            if(!Server.RUNNING_SERVER.get().getVersion().hasFeature(GameVersion.Feature.NAMESPACED_IDS)) {
                throw new IllegalStateException("Cannot create structured item on this version!");
            }

            org.bukkit.inventory.ItemStack bis = Adapter.INSTANCE.get().buildItem(id, count, (byte) 0);
            ItemStack out = new SpigotItem(bis);
            out.loadComponents(components);

            return out;
        }
    }
}
