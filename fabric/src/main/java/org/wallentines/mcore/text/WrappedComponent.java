package org.wallentines.mcore.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.wallentines.mcore.util.ConversionUtil;

import java.util.List;

/**
 * A class which acts as a Minecraft Component while wrapping a MidnightCore Component. Conversion is done only as needed.
 */
public class WrappedComponent implements Component {

    public final org.wallentines.mcore.text.Component internal;

    private Style cachedStyle;
    private ComponentContents cachedContents;
    private List<Component> cachedSiblings;

    /**
     * Creates a new WrappedComponent by wrapping a given MidnightCore component
     * @param internal The MidnightCore component to wrap
     */
    public WrappedComponent(org.wallentines.mcore.text.Component internal) {
        this.internal = internal;
    }

    /**
     * Creates a new WrappedComponent by resolving then wrapping a MidnightCore component
     * @param comp The component to resolve and wrap
     * @param args The context by which to resolve the component
     * @return A new WrappedComponent
     */
    @Deprecated
    public static WrappedComponent resolved(org.wallentines.mcore.text.Component comp, Object... args) {
        return new WrappedComponent(ComponentResolver.resolveComponent(comp, args));
    }

    /**
     * Returns the style information of this component. Will convert if necessary
     * @return The component style.
     */
    @Override
    public @NotNull Style getStyle() {
        if(cachedStyle == null) cachedStyle = ConversionUtil.getStyle(internal);
        return cachedStyle;
    }

    /**
     * Returns the Content information of this component. Will convert if necessary
     * @return The component contents.
     */
    @Override
    public @NotNull ComponentContents getContents() {
        if(cachedContents == null) cachedContents = ContentConverter.convertContent(internal.content);
        return cachedContents;
    }

    /**
     * Returns a list of siblings of this component. All siblings will be WrappedComponents
     * @return The component's siblings.
     */
    @Override
    public @NotNull List<Component> getSiblings() {
        if(cachedSiblings == null) cachedSiblings = internal.children.stream().map(cmp -> (Component) new WrappedComponent(cmp)).toList();
        return cachedSiblings;
    }

    /**
     * Converts the entire component and all of its children, if necessary, and generates a FormattedCharSequence based on it
     * @return A resolved FormattedCharSequence
     */
    @Override
    public @NotNull FormattedCharSequence getVisualOrderText() {

        MutableComponent out = MutableComponent.create(getContents()).withStyle(getStyle());
        for(Component comp : getSiblings()) {
            out.append(comp);
        }

        return out.getVisualOrderText();
    }


}
