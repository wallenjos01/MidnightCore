package org.wallentines.mcore.lang;

import org.wallentines.mcore.text.*;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.codec.DecodeException;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.midnightlib.types.Either;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UnresolvedComponent {

    private final boolean tryParseJSON;
    private final List<Either<String, UnresolvedPlaceholder>> parts = new ArrayList<>();

    // This should be set if the component can be resolved during the parsing phase. (i.e. if there were no placeholders)
    private Component completed;

    private UnresolvedComponent(boolean tryParseJSON) {
        this.tryParseJSON = tryParseJSON;
    }

    /**
     * Resolves this component while flattening all components to a single string
     * @param ctx The context by which to resolve placeholders
     * @return A resolved String
     */
    public String resolveFlat(PlaceholderContext ctx) {

        if(completed != null) {
            return completed.allText();
        }

        StringBuilder out = new StringBuilder();
        for(Either<String, UnresolvedPlaceholder> e : parts) {
            out.append(e.leftOrGet(r -> r.resolve(ctx).leftOrGet(Component::allText)));
        }

        return out.toString();
    }

    /**
     * Resolves this to a Component based on the given context
     * @param ctx The context to use to resolve this component
     * @return A resolved component
     */
    public Component resolve(PlaceholderContext ctx) {

        // Will only be non-null if the component had no placeholders
        if(completed != null) {
            return completed;
        }

        List<Either<String, UnresolvedPlaceholder>> inlined = resolveInline(ctx);

        // Do not attempt to parse JSON if not requested
        if(!tryParseJSON) {
            return resolveConfigText(inlined, ctx);
        }

        // Only attempt to parse as JSON if the component could be JSON
        if (couldBeJson(inlined)) {

            // Make a list of all remaining unresolved placeholders
            List<UnresolvedPlaceholder> unresolved = inlined.stream().filter(Either::hasRight).map(Either::rightOrThrow).toList();
            String toParse = inlined.stream().map(e -> e.leftOrGet(UnresolvedPlaceholder::toRawPlaceholder)).collect(Collectors.joining());

            try {
                SerializeResult<Component> base = ModernSerializer.INSTANCE.deserialize(ConfigContext.INSTANCE, JSONCodec.loadConfig(toParse));
                if (base.isComplete()) {
                    return resolveJSONText(ctx, base.getOrThrow(), unresolved);
                }
            } catch (DecodeException ex) {
                // Component is not JSON, default to config text
            }
        }

        return resolveConfigText(inlined, ctx);
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
        if(completed != null) {
            return completed.toString();
        }
        return "UnresolvedComponent{tryParseJSON=" + tryParseJSON + ",parts=" + parts + "}";
    }

    // Resolve all inline placeholders (placeholders which return a String)
    private List<Either<String, UnresolvedPlaceholder>> resolveInline(PlaceholderContext ctx) {

        List<Either<String, UnresolvedPlaceholder>> out = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for(Either<String, UnresolvedPlaceholder> e : parts) {

            // If the part is a Placeholder
            if(e.hasRight()) {

                // If the placeholder can be resolved to a string
                if (e.rightOrThrow().getSupplier().isInline(ctx)) {

                    current.append(e.rightOrThrow().resolve(ctx).left());

                } else {

                    if (!current.isEmpty()) {
                        out.add(Either.left(current.toString()));
                        current.setLength(0);
                    }

                    out.add(Either.right(e.rightOrThrow()));
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

    private boolean couldBeJson(List<Either<String, UnresolvedPlaceholder>> list) {

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

    private Component resolveConfigText(List<Either<String, UnresolvedPlaceholder>> inlined, PlaceholderContext ctx) {

        List<Either<Component, UnresolvedPlaceholder>> unparsed = inlined.stream().map(e -> {
            if(e.hasLeft()) {
                return Either.<Component, UnresolvedPlaceholder>left(
                        ConfigSerializer.INSTANCE.deserialize(
                                ConfigContext.INSTANCE,
                                new ConfigPrimitive(e.leftOrThrow())
                        ).getOrThrow());
            } else {
                return Either.<Component, UnresolvedPlaceholder>right(e.right());
            }
        }).toList();

        if(unparsed.isEmpty()) {
            return Component.empty();
        }

        int i = 0;

        Component out = unparsed.get(0).leftOrGet(r -> Component.empty());
        if(out == unparsed.get(0).left()) i++;

        for(; i < unparsed.size() ; i++) {
            Either<Component, UnresolvedPlaceholder> pl = unparsed.get(i);
            out = out.addChild(pl.leftOrGet(r -> r.resolve(ctx).rightOrThrow()));
        }

        return out;
    }


    private Component resolveJSONText(PlaceholderContext ctx, Component base, List<UnresolvedPlaceholder> unresolved) throws DecodeException {

        PlaceholderManager manager = new PlaceholderManager();
        for(UnresolvedPlaceholder pl : unresolved) {
            manager.registerSupplier(pl.getId(), pl.getSupplier());
        }

        return resolveContent(ctx, base, manager);
    }

    private Component resolveContent(PlaceholderContext ctx, Component base, PlaceholderManager manager) {

        Component out = base.baseCopy();

        if(base.content instanceof Content.Text) {

            String text = ((Content.Text) base.content).text;

            UnresolvedComponent ent = parse(text, manager).getOrThrow();
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
                        out = out.addChild(part.rightOrThrow().resolve(ctx).rightOrGet(Component::text));
                    }
                }
            }
        }

        for(Component child : base.children) {
            out = out.addChild(resolveContent(ctx, child, manager));
        }

        return out;
    }

    private SerializeResult<UnresolvedComponent> finish() {

        // Check if there are any placeholders to resolve
        if(parts.stream().noneMatch(Either::hasRight)) {

            // No placeholders found; resolve immediately and remove unresolved parts
            completed = resolve(new PlaceholderContext());
            parts.clear();
        }

        return SerializeResult.success(this);
    }

    /**
     * Parses a String into an unresolved component that will not attempt to parse JSON strings.
     * @param str The String to parse
     * @param manager The placeholders to consider
     * @return An unresolved component
     */
    public static SerializeResult<UnresolvedComponent> parse(String str, PlaceholderManager manager) {

        return parse(str, manager, false);
    }

    /**
     * Parses a String into an unresolved component
     * @param str The String to parse
     * @param manager The placeholders to consider
     * @param tryParseJSON Whether an attempt should be made to parse JSON-formatted strings as components at
     *                     resolution time. This allows placeholders to be used in JSON components, at a performance
     *                     cost for each resolution.
     * @return An unresolved component
     */
    public static SerializeResult<UnresolvedComponent> parse(String str, PlaceholderManager manager, boolean tryParseJSON) {

        return parse(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)))), null, manager, tryParseJSON);

    }

    static SerializeResult<UnresolvedComponent> parse(BufferedReader reader, Character earlyTerminate, PlaceholderManager manager, boolean tryParseJSON) {

        try {

            UnresolvedComponent out = new UnresolvedComponent(tryParseJSON);
            StringBuilder currentString = new StringBuilder();

            int chara;
            reader.mark(2);

            while ((chara = reader.read()) != -1) {

                if (chara == '%') {

                    if (reader.read() == '%') {
                        currentString.append('%');

                    } else {
                        reader.reset();
                        SerializeResult<UnresolvedPlaceholder> placeholder = UnresolvedPlaceholder.parse(reader, manager, tryParseJSON);
                        if(!placeholder.isComplete()) {
                            return SerializeResult.failure("Unable to parse entry! " + placeholder.getError());
                        }

                        if (!currentString.isEmpty()) {
                            out.parts.add(Either.left(currentString.toString()));
                            currentString.setLength(0);
                        }

                        out.parts.add(Either.right(placeholder.getOrThrow()));
                    }

                } else if (earlyTerminate != null && chara == earlyTerminate) {

                    if (!currentString.isEmpty()) {
                        out.parts.add(Either.left(currentString.toString()));
                        currentString.setLength(0);
                    }

                    return out.finish();

                } else {
                    currentString.appendCodePoint(chara);
                }
                reader.mark(2);
            }

            if (!currentString.isEmpty()) {
                out.parts.add(Either.left(currentString.toString()));
                currentString.setLength(0);
            }

            return out.finish();

        } catch (IOException ex) {

            return SerializeResult.failure("Unable to parse entry! Encountered IOException! " + ex.getMessage());
        }
    }


}
