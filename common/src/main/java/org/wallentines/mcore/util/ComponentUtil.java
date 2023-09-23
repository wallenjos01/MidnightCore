package org.wallentines.mcore.util;

import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.Content;

import java.util.function.Function;

public class ComponentUtil {

    public static Component editText(Component component, Function<String, String> editor) {

        Component out = component.baseCopy();

        Content cnt = component.content;
        if(cnt instanceof Content.Text) {
            out = out.withContent(new Content.Text(editor.apply(((Content.Text) cnt).text)));
        }

        for(Component cmp : out.children) {
            out.addChild(editText(cmp, editor));
        }

        return out;
    }

}
