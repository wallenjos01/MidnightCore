package org.wallentines.mcore.text;

import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.types.Either;

import java.util.ArrayList;
import java.util.List;

public class MutableComponent {

    /**
     * The color of the component
     */
    public Color color;

    /**
     * Whether the component is bold
     */
    public Boolean bold;

    /**
     * Whether the component is italic
     */
    public Boolean italic;

    /**
     * Whether the component is underlined
     */
    public Boolean underlined;

    /**
     * Whether the component has strikethrough
     */
    public Boolean strikethrough;

    /**
     * Whether the component is obfuscated
     */
    public Boolean obfuscated;

    /**
     * If set to true, the component will have its color reset to the default for the given context.
     */
    public Boolean reset;

    /**
     * The font of the component
     */
    public Identifier font;

    /**
     * The text that will be inserted when the player shift-clicks the component
     */
    public String insertion;

    /**
     * The event which occurs when a player hovers over the component
     */
    public HoverEvent<?> hoverEvent;

    /**
     * The event which occurs when a player clicks on the component
     */
    public ClickEvent clickEvent;

    /**
     * The content which will be displayed to the player
     */
    public final Content content;

    /**
     * The 'extra' components as children of the component
     */
    public final List<Either<Component, MutableComponent>> children = new ArrayList<>();

    public MutableComponent(Content content) {
        this.content = content;
    }

    public static MutableComponent fromComponent(Component other) {
        MutableComponent out = new MutableComponent(other.content);
        out.color = other.color;
        out.bold = other.bold;
        out.italic = other.italic;
        out.underlined = other.underlined;
        out.strikethrough = other.strikethrough;
        out.obfuscated = other.obfuscated;
        out.reset = other.reset;
        out.font = other.font;
        out.insertion = other.insertion;
        out.hoverEvent = other.hoverEvent;
        out.clickEvent = other.clickEvent;
        other.children.forEach(out::addChild);

        return out;
    }

    public static MutableComponent empty() {
        return new MutableComponent(new Content.Text(""));
    }

    public static MutableComponent text(String text) {
        return new MutableComponent(new Content.Text(text));
    }

    public Component toComponent() {

        return new Component(color, bold, italic, underlined, strikethrough, obfuscated, reset, font, insertion, hoverEvent, clickEvent, content, children.stream().map(e -> e.leftOrGet(MutableComponent::toComponent)).toList());
    }

    public MutableComponent getChild(int index) {
        return children.get(index).rightOrGet(c -> {

            MutableComponent out = MutableComponent.fromComponent(c);
            children.set(index, Either.right(out));
            return out;
        });
    }

    public void addChild(MutableComponent cmp) {
        this.children.add(Either.right(cmp));
    }

    public void addChild(Component cmp) {
        this.children.add(Either.left(cmp));
    }

}
