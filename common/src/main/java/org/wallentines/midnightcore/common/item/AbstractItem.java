package org.wallentines.midnightcore.common.item;

import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MHoverEvent;
import org.wallentines.midnightcore.api.text.MStyle;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractItem implements MItemStack {

    private final Identifier typeId;
    protected int count;
    protected ConfigSection tag;

    protected AbstractItem(Identifier typeId, int count, ConfigSection tag) {
        this.typeId = typeId;
        this.count = count;
        this.tag = tag;
    }

    @Override
    public Identifier getType() {
        return typeId;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public ConfigSection getTag() {
        return tag;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
        update();
    }

    @Override
    public void setTag(ConfigSection tag) {
        this.tag = tag;
        update();
    }

    @Override
    public MComponent getName() {
        MComponent out = null;
        if(tag != null && tag.has("display", ConfigSection.class)) {
            ConfigSection display = tag.getSection("display");
            if(display.has("Name", String.class)) {
                out = MComponent.SERIALIZER.deserialize(JsonConfigProvider.INSTANCE.loadFromString(display.getString("Name")));
            }
        }

        if(out == null) {
            out = getTranslationComponent();
        } else {
            MComponent swap = new MTextComponent("").withStyle(new MStyle().withItalic(true));
            swap.addChild(out);
            out = swap;
        }

        out.setHoverEvent(MHoverEvent.createItemHover(this));
        return out;
    }

    @Override
    public void setName(MComponent component) {
        MComponent newComp = new MTextComponent("").withStyle(MStyle.ITEM_NAME_BASE).withChild(component);
        if(tag == null) tag = new ConfigSection();

        tag.getOrCreateSection("display").set("Name", MComponent.SERIALIZER.serialize(newComp).toString());
        update();
    }

    @Override
    public List<MComponent> getLore() {
        if(tag == null || !tag.has("display", ConfigSection.class)) return null;

        ConfigSection display = tag.getSection("display");
        if(!display.has("Lore", List.class)) return null;

        return display.getListFiltered("Lore", MComponent.class);
    }

    @Override
    public void setLore(List<MComponent> lore) {

        List<String> newLore = new ArrayList<>();
        for(MComponent cmp : lore) {
            newLore.add(new MTextComponent("").withStyle(MStyle.ITEM_LORE_BASE).withChild(cmp.copy()).toString());
        }

        if(tag == null) tag = new ConfigSection();
        tag.getOrCreateSection("display").set("Lore", newLore);
        update();
    }

    @Override
    public MItemStack copy() {

        return Builder.of(typeId).withAmount(count).withTag(tag.copy()).build();
    }

    @Override
    public String saveToNBT() {
        return MItemStack.toNBT(new ConfigSection().with("id", typeId).with("Count", count).with("tag", tag));
    }

    protected abstract MComponent getTranslationComponent();
}
