package org.wallentines.midnightcore.velocity.item;

import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.common.item.AbstractItem;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

public class DummyItem extends AbstractItem {

    public DummyItem(Identifier typeId, int count, ConfigSection tag) {
        super(typeId, count, tag);
    }

    @Override
    public void update() {

    }

    @Override
    protected MComponent getTranslationComponent() {
        return null;
    }
}
