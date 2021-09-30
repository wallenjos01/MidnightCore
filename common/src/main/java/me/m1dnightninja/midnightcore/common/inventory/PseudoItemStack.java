package me.m1dnightninja.midnightcore.common.inventory;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.math.Color;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.MHoverEvent;
import me.m1dnightninja.midnightcore.api.text.MStyle;

import java.util.List;

public class PseudoItemStack extends MItemStack {

    protected PseudoItemStack(MIdentifier type, int count) {
        super(type, count);
    }

    public PseudoItemStack(MIdentifier type, int count, ConfigSection tag) {
        super(type, count, tag);
    }

    @Override
    public void update() {

    }

    @Override
    public MComponent getName() {

        ConfigSection display = tag.getOrDefault("display", null, ConfigSection.class);

        MComponent cmp;
        if(display != null && display.has("Name")) {

            cmp = MComponent.createTranslatableComponent("").withStyle(new MStyle().withItalic(Boolean.TRUE));
            cmp.addChild(MComponent.Serializer.fromJson(display.getString("Name")));

        } else {

            String desc = type == null ? "item.unregistered_sadface" : "item." + type.getNamespace() + "." + type.getPath().replace('/', '.');
            cmp = MComponent.createTranslatableComponent(desc);
        }

        if(tag.has("Enchantments", List.class)) {
            cmp.getStyle().fill(new MStyle().withColor(Color.fromRGBI(11)));
        }

        return cmp.withHoverEvent(MHoverEvent.createItemHover(this));
    }
}
