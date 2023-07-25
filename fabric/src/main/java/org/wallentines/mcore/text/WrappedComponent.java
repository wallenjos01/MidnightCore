package org.wallentines.mcore.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.util.ConversionUtil;

import java.util.List;

public class WrappedComponent implements Component {

    public final org.wallentines.mcore.text.Component internal;

    private Style cachedStyle;
    private ComponentContents cachedContents;
    private List<Component> cachedSiblings;

    public WrappedComponent(org.wallentines.mcore.text.Component internal) {
        this.internal = internal;
    }

    public static Component resolved(org.wallentines.mcore.text.Component comp, Player player) {
        return new WrappedComponent(ComponentResolver.resolveComponent(comp, player));
    }

    @Override
    public @NotNull Style getStyle() {
        if(cachedStyle == null) cachedStyle = ConversionUtil.getStyle(internal);
        return cachedStyle;
    }

    @Override
    public @NotNull ComponentContents getContents() {
        if(cachedContents == null) cachedContents = ContentConverter.convertContent(internal.content);
        return cachedContents;
    }

    @Override
    public @NotNull List<Component> getSiblings() {
        if(cachedSiblings == null) cachedSiblings = internal.children.stream().map(cmp -> (Component) new WrappedComponent(cmp)).toList();
        return cachedSiblings;
    }

    @Override
    public @NotNull FormattedCharSequence getVisualOrderText() {

        MutableComponent out = MutableComponent.create(getContents()).withStyle(getStyle());
        for(Component comp : getSiblings()) {
            out.append(comp);
        }

        return out.getVisualOrderText();
    }


}
