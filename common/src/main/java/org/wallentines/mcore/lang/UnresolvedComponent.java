package org.wallentines.mcore.lang;

import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.text.*;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.codec.DecodeException;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.types.Either;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A data type representing a component which has been parsed but not resolved
 */
public class UnresolvedComponent {

    private final boolean tryParseJSON;
    private final List<Either<String, UnresolvedPlaceholder>> parts;
    private final PlaceholderContext context;

    // This should be set if the component can be resolved during the parsing phase. (i.e. if there were no placeholders)
    private Component completed;

    private UnresolvedComponent(boolean tryParseJSON, PlaceholderContext context, List<Either<String, UnresolvedPlaceholder>> entries) {
        this.tryParseJSON = tryParseJSON;
        this.context = context;
        this.parts = List.copyOf(entries);
    }

    private UnresolvedComponent(Component completed) {
        this.tryParseJSON = false;
        this.context = new PlaceholderContext();
        this.parts = null;
        this.completed = completed;
    }

    public PlaceholderContext getContext() {
        return context;
    }

    public boolean isComplete() {
        return completed != null;
    }

    /**
     * Resolves this component while flattening all components to a single string, using the global PlaceholderManager
     * @param ctx The context by which to resolve placeholders
     * @return A resolved String
     */
    public String resolveFlat(PlaceholderContext ctx) {
        return resolveFlat(PlaceholderManager.INSTANCE, ctx);
    }

    /**
     * Resolves this component while flattening all components to a single string
     * @param manager The placeholders to consider
     * @param ctx The context by which to resolve placeholders
     * @return A resolved String
     */
    public String resolveFlat(PlaceholderManager manager, PlaceholderContext ctx) {

        if(completed != null) {
            return completed.allText();
        }

        StringBuilder out = new StringBuilder();
        for(Either<String, UnresolvedPlaceholder> e : parts) {
            out.append(e.leftOrGet(r -> r.resolve(manager, ctx).leftOrGet(Component::allText)));
        }

        return out.toString();
    }

    /**
     * Resolves this to a Component based on the internal context
     * @return A resolved component
     */
    public Component resolve() {
        return resolve(PlaceholderManager.INSTANCE, context);
    }

    /**
     * Resolves this to a Component based on the internal context
     * @param obj Objects to add to the placeholder context for resolution
     * @return A resolved component
     */
    public Component resolveFor(Object... obj) {
        PlaceholderContext ctx = context.copy();
        for(Object o : obj) ctx.addValue(o);
        return resolve(PlaceholderManager.INSTANCE, ctx);
    }

    /**
     * Resolves this to a Component based on the given context
     * @param ctx The context to use to resolve this component
     * @return A resolved component
     */
    public Component resolve(PlaceholderContext ctx) {

        if(ctx != null) ctx = ctx.and(context);
        return resolve(PlaceholderManager.INSTANCE, ctx);
    }

    /**
     * Resolves this to a Component based on the given context
     * @param manager The placeholder manager containing placeholders
     * @return A resolved component
     */
    public Component resolve(PlaceholderManager manager) {

        return resolve(manager, context);
    }

    /**
     * Resolves this to a Component based on the given context
     * @param manager The placeholders to consider
     * @param ctx The context to use to resolve this component
     * @return A resolved component
     */
    public Component resolve(PlaceholderManager manager, PlaceholderContext ctx) {

        // Will only be non-null if the component had no placeholders
        if(completed != null) {
            return completed;
        }

        List<Either<String, Component>> inlined = resolvePlaceholders(manager, ctx);

        // Do not attempt to parse JSON if not requested
        if(!tryParseJSON) {
            return resolveConfigText(inlined);
        }

        // Only attempt to parse as JSON if the component could be JSON
        if (couldBeJson(inlined)) {

            StringBuilder toParse = new StringBuilder();

            PlaceholderContext finalContext = new PlaceholderContext();
            int index = 0;
            for(Either<String, Component> e : inlined) {
                if(e.hasLeft()) {
                    toParse.append(e.leftOrThrow());
                } else {
                    // Components to insert should be put in place as a pseudo-placeholder in the format %~:INDEX%
                    String id = "~:" + (index++);
                    toParse.append('%').append(id).append('%');
                    finalContext.addValue(CustomPlaceholder.of(id, e.rightOrThrow()));
                }
            }

            try {
                SerializeResult<Component> base = ModernSerializer.INSTANCE.deserialize(ConfigContext.INSTANCE, JSONCodec.loadConfig(toParse.toString()), GameVersion.MAX);
                if (base.isComplete()) {
                    return finalizeJSON(manager, finalContext, base.getOrThrow());
                }
            } catch (DecodeException ex) {
                // Component is not JSON, default to config text
            }
        }

        return resolveConfigText(inlined);
    }

    /**
     * Returns a raw representation of this unresolved component, as if it weren't parsed
     * @return A raw representation of this unresolved component (i.e. Hello, %username%)
     */
    public String toRaw() {
        if(completed != null) {
            return completed.toConfigText();
        }

        return parts.stream().map(e -> e.leftOrGet(UnresolvedPlaceholder::toRawPlaceholder)).collect(Collectors.joining());
    }

    public UnresolvedComponent copy() {
        return new UnresolvedComponent(tryParseJSON, context.copy(), parts);
    }

    public UnresolvedComponent copyWithContext(PlaceholderContext context) {
        return new UnresolvedComponent(tryParseJSON, context, parts);
    }

    public UnresolvedComponent copyWith(Object... values) {
        return new UnresolvedComponent(tryParseJSON, context.copy().withValues(values), parts);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnresolvedComponent that = (UnresolvedComponent) o;
        if(completed != null) {
            return that.completed != null;
        }
        return tryParseJSON == that.tryParseJSON && Objects.equals(parts, that.parts);
    }


    @Override
    public int hashCode() {
        if(completed != null) {
            return completed.hashCode();
        }
        return Objects.hash(tryParseJSON, parts);
    }

    @Override
    public String toString() {
        if (completed != null) {
            return completed.toString();
        }
        return "UnresolvedComponent{tryParseJSON=" + tryParseJSON + ",parts=" + parts + "}";
    }

    public static class Builder {

        private List<Either<String, UnresolvedPlaceholder>> parts = new ArrayList<>();
        private PlaceholderContext context = new PlaceholderContext();
        private boolean tryParseJSON;
        private boolean completed = true;

        public Builder append(String literal) {
            parts.add(Either.left(literal));
            return this;
        }
        public Builder append(UnresolvedPlaceholder placeholder) {
            parts.add(Either.right(placeholder));
            completed = false;
            return this;
        }

        public Builder append(UnresolvedComponent component) {

            if(component.isComplete()) {
                append(component.toRaw());
            } else if(component.parts != null) {
                parts.addAll(component.parts);
                if (completed) completed = component.isComplete();
            }
            return this;
        }

        public Builder appendPlaceholder(String placeholder) {
            append(new UnresolvedPlaceholder(placeholder));
            return this;
        }

        public Builder appendPlaceholder(String placeholder, String argument) {
            append(new UnresolvedPlaceholder(placeholder, builder().append(argument).build()));
            return this;
        }

        public Builder appendPlaceholder(String placeholder, UnresolvedComponent argument) {
            append(new UnresolvedPlaceholder(placeholder, argument));
            return this;
        }

        public Builder tryParseJSON() {
            return tryParseJSON(true);
        }

        public Builder tryParseJSON(boolean value) {
            this.tryParseJSON = value;
            return this;
        }

        public Builder withContextValue(Object o) {
            context.addValue(o);
            return this;
        }

        public Builder withContext(PlaceholderContext context) {
            this.context = context;
            return this;
        }

        public UnresolvedComponent build() {

            UnresolvedComponent out = new UnresolvedComponent(tryParseJSON, context, parts);
            if(completed) {
                out = UnresolvedComponent.completed(out.resolve());
            }

            return out;
        }

    }
    public static UnresolvedComponent.Builder builder() {
        return new Builder();
    }

    public static UnresolvedComponent empty() {
        return completed(Component.empty());
    }

    public static UnresolvedComponent completed(Component component) {
        return new UnresolvedComponent(component);
    }

    /**
     * Parses a String into an unresolved component that will not attempt to parse JSON strings.
     * @param str The String to parse
     * @return An unresolved component
     */
    public static SerializeResult<UnresolvedComponent> parse(String str) {

        return parse(str, false);
    }


    /**
     * Parses a String into an unresolved component
     * @param str The String to parse
     * @param tryParseJSON Whether an attempt should be made to parse JSON-formatted strings as components at
     *                     resolution time. This allows placeholders to be used in JSON components, at a performance
     *                     cost for each resolution.
     * @return An unresolved component
     */
    public static SerializeResult<UnresolvedComponent> parse(String str, boolean tryParseJSON) {

        return parse(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)), null, tryParseJSON);

    }

    static SerializeResult<UnresolvedComponent> parse(BufferedReader reader, Character earlyTerminate, boolean tryParseJSON) {

        try {

            UnresolvedComponent.Builder out = new UnresolvedComponent.Builder().tryParseJSON(tryParseJSON);

            CharBuffer buffer = CharBuffer.allocate(1024);
            StringBuilder currentString = new StringBuilder();

            while(true) {

                buffer.clear();

                reader.mark(1024);
                int bytesRead = reader.read(buffer);

                if(bytesRead == -1) {
                    break;
                }
                String str = buffer.rewind().toString().substring(0, bytesRead);

                int earlyIndex = Integer.MAX_VALUE;
                if(earlyTerminate != null) {
                    int idx = str.indexOf(earlyTerminate);
                    if(idx > -1) {
                        earlyIndex = idx;
                    }
                }

                int index = str.indexOf('%');
                if(earlyIndex < index) {
                    reader.reset();
                    reader.skip(earlyIndex+1);
                    if(earlyIndex > 0) {
                        out.append(str.substring(0, earlyIndex));
                    }
                    break;
                }

                if(index > -1) {
                    if(index > 0) {
                        currentString.append(str, 0, index);
                    }

                    reader.reset();
                    reader.skip(index);

                    if(str.charAt(index + 1) == '%') {
                        currentString.append("%");
                        reader.skip(1);

                    } else {

                        SerializeResult<UnresolvedPlaceholder> placeholder = UnresolvedPlaceholder.parse(reader, tryParseJSON);
                        if(!placeholder.isComplete()) {
                            return SerializeResult.failure("Unable to parse entry! " + placeholder.getError());
                        }

                        if (!currentString.isEmpty()) {
                            out.append(currentString.toString());
                            currentString.setLength(0);
                        }

                        out.append(placeholder.getOrThrow());
                    }
                } else {

                    out.append(str);

                }
            }

            buffer.clear();
            return SerializeResult.success(out.build());

        } catch (IOException ex) {

            return SerializeResult.failure("Unable to parse entry! Encountered IOException! " + ex.getMessage());
        }
    }

    // Resolve all placeholders
    private List<Either<String, Component>> resolvePlaceholders(PlaceholderManager manager, PlaceholderContext ctx) {

        List<Either<String, Component>> out = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for(Either<String, UnresolvedPlaceholder> e : parts) {

            // If the part is a Placeholder
            if(e.hasRight()) {

                Either<String, Component> resolved = e.rightOrThrow().resolve(manager, ctx);

                // If the placeholder was resolved to a string
                if(resolved.hasLeft()) {
                    current.append(resolved.leftOrThrow());
                } else {
                    if (!current.isEmpty()) {
                        out.add(Either.left(current.toString()));
                        current.setLength(0);
                    }

                    out.add(Either.right(resolved.rightOrThrow()));
                }

                // If the part is a String
            } else {
                current.append(e.left());
            }
        }

        if(!current.isEmpty()) {
            out.add(Either.left(current.toString()));
        }

        return out;
    }


    private boolean couldBeJson(List<Either<String, Component>> list) {

        int length = list.size();
        if(length == 0) {
            return false;
        }

        // It cannot be JSON if it starts or ends with a placeholder
        if(list.get(0).hasRight() || list.get(length - 1).hasRight()) {
            return false;
        }

        return list.get(0).leftOrThrow().stripLeading().startsWith("{") &&
                list.get(length - 1).leftOrThrow().stripTrailing().endsWith("}");
    }


    private Component resolveConfigText(List<Either<String, Component>> inlined) {

        MutableComponent out = null;
        for(Either<String, Component> cmp : inlined) {

            if(cmp.hasLeft()) {
                MutableComponent text = MutableComponent.fromComponent(ConfigSerializer.INSTANCE.deserialize(ConfigContext.INSTANCE, new ConfigPrimitive(cmp.leftOrThrow())).getOrThrow());
                if(out == null) {
                    out = text;
                } else {
                    out.addChild(text);
                }

            } else {

                MutableComponent next = MutableComponent.fromComponent(cmp.rightOrThrow());
                if(out == null) {
                    out = next;
                } else {
                    if (out.children.isEmpty()) {
                        out.addChild(next);
                    } else {
                        out.getChild(out.children.size() - 1).addChild(next);
                    }
                }
            }
        }

        if(out == null) {
            return Component.empty();
        }

        return out.toComponent();
    }


    private Component finalizeJSON(PlaceholderManager manager, PlaceholderContext ctx, Component base) throws DecodeException {

        Component out = base.baseCopy();

        if(base.content instanceof Content.Text) {

            String text = ((Content.Text) base.content).text;
            UnresolvedComponent ent = parse(text).getOrThrow();
            if(!ent.parts.isEmpty()) {

                int start = 0;
                if(ent.parts.get(0).hasLeft()) {
                    start++;
                    out = out.withContent(new Content.Text(ent.parts.get(0).left()));
                } else {
                    out = out.withContent(new Content.Text(""));
                }

                for(int i = start ; i < ent.parts.size() ; i++) {

                    Either<String, UnresolvedPlaceholder> part = ent.parts.get(i);
                    if(part.hasLeft()) {
                        out = out.addChild(Component.text(part.left()));
                    } else {
                        out = out.addChild(part.rightOrThrow().resolve(manager, ctx).rightOrGet(Component::text));
                    }
                }
            }
        }

        for(Component child : base.children) {
            out = out.addChild(finalizeJSON(manager, ctx, child));
        }

        return out;
    }

    /**
     * A MidnightCFG serializer for unresolved components. Expects a string input and supplies a string output
     */
    public static final Serializer<UnresolvedComponent> SERIALIZER = new Serializer<>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, UnresolvedComponent value) {
            return SerializeResult.success(context.toString(value.toRaw()));
        }

        @Override
        public <O> SerializeResult<UnresolvedComponent> deserialize(SerializeContext<O> context, O value) {
            if(!context.isString(value)) {
                return SerializeResult.failure("Unable to parse unresolved component! Expected a String!");
            }
            return parse(context.asString(value));
        }
    };

}
