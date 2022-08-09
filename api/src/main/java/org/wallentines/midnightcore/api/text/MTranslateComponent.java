package org.wallentines.midnightcore.api.text;

import org.wallentines.midnightlib.config.ConfigSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MTranslateComponent extends MComponent {

    private final List<MComponent> with;

    public MTranslateComponent(String content, MComponent... with) {
        this(content, Arrays.asList(with));
    }

    public MTranslateComponent(String content, List<MComponent> with) {

        super(MComponent.ComponentType.TRANSLATE, content);
        this.with = with;
    }

    public List<MComponent> getArgs() {
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

    @Override
    protected void onSerialize(ConfigSection sec) {

        if(with == null) return;

        List<ConfigSection> out = new ArrayList<>();
        with.forEach(w -> out.add(MComponent.SERIALIZER.serialize(w)));

        sec.set("with", out);
    }

}
