package org.wallentines.mcore.text;

import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.types.Either;


/**
 * A {@link Serializer} which serializes components into legacy Strings for clients/contexts which do not
 * support modern text components
 */
public class LegacySerializer implements Serializer<Component> {


    /**
     * Contains serializers for the default content types
     */
    //public static final StringRegistry<InlineContentSerializer<?>> CONTENT_SERIALIZERS = new StringRegistry<>();

    /**
     * A LegacySerializer instance which uses the color code character and does not support hex. For true legacy text
     */
    public static final LegacySerializer INSTANCE = new LegacySerializer('\u00A7', false);

    /**
     * A LegacySerializer instance which uses an ampersand for color codes and has hex support. For text stored in
     * config files.
     */
    public static final LegacySerializer CONFIG_INSTANCE = new LegacySerializer('&', true);

    private final Character colorChar;
    private final boolean hexSupport;

    /**
     * Creates a LegacySerializer with the given color character, and optional rgb color support
     * @param colorChar The character which should prefix all color codes
     * @param hexSupport Whether hex color codes which appear after the color character (i.e. &#112233 if the color
     *                   character is '&') should be parsed or written.
     */
    public LegacySerializer(Character colorChar, boolean hexSupport) {
        this.colorChar = colorChar;
        this.hexSupport = hexSupport;
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context, Component value) {

        String style = toLegacyStyle(value);
        StringBuilder out = new StringBuilder(style);
        out.append(PlainSerializer.serializeContent(value.content));

        for(Component child : value.children) {

            SerializeResult<O> childRes = serialize(context, child);
            if(!childRes.isComplete()) {
                return childRes;
            }

            out.append(context.asString(childRes.getOrThrow()));
        }

        return SerializeResult.success(context.toString(out.toString()));
    }

    @Override
    public <O> SerializeResult<Component> deserialize(SerializeContext<O> context, O value) {

        if(!context.isString(value)) {
            return SerializeResult.failure("Unable to deserialize " + value + "! Expected a String!");
        }

        return SerializeResult.success(parsePlainText(context.asString(value)));
    }


    private String toLegacyStyle(Component component) {

        StringBuilder out = new StringBuilder();

        if(component.color != null) out.append(colorChar).append(hexSupport ?
                                    '#' + component.color.toPlainHex() :
                                    Integer.toHexString(component.color.toRGBI()));

        if(component.bold != null && component.bold)                   out.append(colorChar).append("l");
        if(component.italic  != null && component.italic)              out.append(colorChar).append("o");
        if(component.underlined != null && component.underlined)       out.append(colorChar).append("n");
        if(component.strikethrough != null && component.strikethrough) out.append(colorChar).append("m");
        if(component.obfuscated != null && component.obfuscated)       out.append(colorChar).append("k");

        return out.toString();
    }


    private Component parsePlainText(String content) {

        MutableComponent out = MutableComponent.empty();

        StringBuilder currentString = new StringBuilder();
        MutableComponent currentComponent = out;

        for(int i = 0 ; i < content.length() ; i++) {

            char c = content.charAt(i);
            if(c == colorChar && i < content.length() - 1) {
                char next = content.charAt(i + 1);

                if(next == colorChar) {
                    currentString.append(colorChar);
                    i += 1;
                }
                else if ((next >= '0' && next <= '9') || (next >= 'a' && next <= 'f')) {

                    int legacy = Integer.parseInt(String.valueOf(next), 16);
                    if(!currentString.isEmpty()) {
                        currentComponent.content = new Content.Text(currentString.toString());
                        currentString = new StringBuilder();

                        currentComponent = MutableComponent.empty();
                        out.addChild(currentComponent);
                    }

                    i += 1;
                    currentComponent.color = Color.fromRGBI(legacy);

                } else if(next == 'r') {

                    if(!currentString.isEmpty()) {
                        currentComponent.content = new Content.Text(currentString.toString());
                        currentString = new StringBuilder();

                        currentComponent = MutableComponent.empty();
                        out.addChild(currentComponent);
                    }
                    i += 1;

                    currentComponent.reset = true;

                } else if(hexSupport && next == '#' && i < content.length() - 8) {

                    String hex = content.substring(i + 2, i + 8);
                    if(!currentString.isEmpty()) {
                        currentComponent.content = new Content.Text(currentString.toString());
                        currentString = new StringBuilder();

                        currentComponent = MutableComponent.empty();
                        out.addChild(currentComponent);
                    }

                    i += 7;
                    currentComponent.color = Color.parse(hex).get().orElse(null);

                } else {

                    if(!currentString.isEmpty()) {
                        currentComponent.content = new Content.Text(currentString.toString());
                        currentString = new StringBuilder();

                        MutableComponent child = MutableComponent.empty();
                        currentComponent.addChild(child);

                        currentComponent = child;
                    }

                    switch (next) {
                        case 'l': { currentComponent.bold = true; i += 1; break; }
                        case 'o': { currentComponent.italic = true; i += 1; break; }
                        case 'n': { currentComponent.underlined = true; i += 1; break; }
                        case 'm': { currentComponent.strikethrough = true; i += 1; break; }
                        case 'k': { currentComponent.obfuscated = true; i += 1; break; }
                    }
                }
            } else {
                currentString.append(c);
            }
        }

        if(!currentString.isEmpty()) {
            currentComponent.content = new Content.Text(currentString.toString());
        }

        if(!out.children.isEmpty() && out.hasFormatting()) {
            MutableComponent child = MutableComponent.empty();
            child.content = out.content;
            child.bold = out.bold;
            child.italic = out.italic;
            child.underlined = out.underlined;
            child.strikethrough = out.strikethrough;
            child.obfuscated = out.obfuscated;

            out.content = new Content.Text("");
            out.bold = null;
            out.italic = null;
            out.underlined = null;
            out.strikethrough = null;
            out.obfuscated = null;

            out.children.add(0, Either.right(child));
        }

        return out.toComponent();

//        MutableComponent first = out.get(0);
//        if(out.size() > 1 && first.hasFormatting()) {
//
//            MutableComponent child = MutableComponent.empty();
//            child.content = first.content;
//            child.bold = first.bold;
//            child.italic = first.italic;
//            child.underlined = first.underlined;
//            child.strikethrough = first.strikethrough;
//            child.obfuscated = first.obfuscated;
//
//            first.content = new Content.Text("");
//            first.bold = null;
//            first.italic = null;
//            first.underlined = null;
//            first.strikethrough = null;
//            first.obfuscated = null;
//
//            first.addChild(child);
//        }
//
//        for(int i = 1; i < out.size() ; i++) {
//            first.addChild(out.get(i));
//        }
//
//        return first.toComponent();
    }


}
