package org.wallentines.midnightcore.api.text;

import org.wallentines.midnightlib.config.ConfigSection;

public class MTextComponent extends MComponent {

    public MTextComponent(String content) {
        super(ComponentType.TEXT, content);
    }

    @Override
    public int contentLength() {
        return content.length();
    }

    @Override
    protected MComponent baseCopy() {
        return new MTextComponent(content);
    }

    @Override
    protected void onSerialize(ConfigSection sec) { }
}
