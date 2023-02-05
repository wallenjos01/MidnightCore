package org.wallentines.midnightcore.api.text;

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

}
