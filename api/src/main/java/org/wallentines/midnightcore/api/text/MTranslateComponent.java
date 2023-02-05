package org.wallentines.midnightcore.api.text;

import java.util.Arrays;
import java.util.Collection;

public class MTranslateComponent extends MComponent {

    private final Collection<MComponent> with;

    public MTranslateComponent(String content, MComponent... with) {
        this(content, Arrays.asList(with));
    }

    public MTranslateComponent(String content, Collection<MComponent> with) {

        super(MComponent.ComponentType.TRANSLATE, content);
        this.with = with;
    }

    public Collection<MComponent> getArgs() {
        return with;
    }

    @Override
    public int contentLength() {
        return 0;
    }

    @Override
    protected MComponent baseCopy() {
        return new MTranslateComponent(content, with);
    }


}
