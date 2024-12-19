package org.wallentines.mcore.text;

import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

/**
 * A {@link Serializer} which serializes components into plain text, disregarding color or formatting information in the process
 */
public class PlainSerializer implements Serializer<Component> {

    /**
     * The global plain serializer instance
     */
    public static final PlainSerializer INSTANCE = new PlainSerializer();

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context, Component value) {

        StringBuilder str = new StringBuilder(serializeContent(value.content));
        for(Component comp : value.children) {
            SerializeResult<O> child = serialize(context, comp);
            if(!child.isComplete()) {
                return child;
            }
            str.append(context.asString(child.getOrThrow()).getOrThrow());
        }

        return SerializeResult.success(context.toString(str.toString()));
    }

    @Override
    public <O> SerializeResult<Component> deserialize(SerializeContext<O> context, O value) {
        return context.asString(value).flatMap(Component::text);
    }


    public static String serializeContent(Content content) {
        switch (content.type.getId()) {
            case "text":
                return ((Content.Text) content).text;
            case "translate":
                return ((Content.Translate) content).key;
            case "keybind":
                return ((Content.Keybind) content).key;
            default:
                return "";
        }
    }

}
