package org.wallentines.mcore.lang;

import org.wallentines.mcore.text.Component;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.midnightlib.types.Either;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Objects;

/**
 * A data type which represents a placeholder which has been parsed but not resolved
 */
public class UnresolvedPlaceholder {

    private final String id;
    private final UnresolvedComponent argument;

    /**
     * Constructs a new unresolved placeholder
     * @param id The ID of the placeholder
     * @param argument The argument for the placeholder, if applicable
     */
    public UnresolvedPlaceholder(String id, UnresolvedComponent argument) {
        this.id = id;
        this.argument = argument;
    }


    /**
     * Returns the parsed placeholder ID
     * @return The placeholder ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the parsed placeholder argument
     * @return The placeholder argument
     */
    public UnresolvedComponent getArgument() {
        return argument;
    }

    /**
     * Generates a raw placeholder representation of this placeholder, as if it weren't parsed at all
     * @return A raw placeholder
     */
    public String toRawPlaceholder() {

        StringBuilder out = new StringBuilder("%");
        out.append(id);
        if(argument != null) {
            out.append("<").append(argument).append(">");
        }
        out.append("%");

        return out.toString();
    }

    /**
     * Resolves this placeholder according to the given context
     * @param manager The placeholders to consider
     * @param ctx The context by which to resolve this placeholder
     * @return Either a String or a Component, depending on the placeholder supplier
     */
    public Either<String, Component> resolve(PlaceholderManager manager, PlaceholderContext ctx) {

        if(argument != null) ctx = ctx.copy(argument.resolve(manager, ctx));

        Either<String, Component> out = ctx.getCustomPlaceholder(id);
        if(out == null) {
            PlaceholderSupplier supp = manager.getPlaceholderSupplier(id);
            if(supp == null || (out = supp.get(ctx)) == null) {
                return Either.left(toRawPlaceholder());
            }
        }

        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnresolvedPlaceholder that = (UnresolvedPlaceholder) o;
        return Objects.equals(id, that.id) && Objects.equals(argument, that.argument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, argument);
    }

    @Override
    public String toString() {
        return toRawPlaceholder();
    }

    static SerializeResult<UnresolvedPlaceholder> parse(BufferedReader reader, boolean tryParseJSON) {

        try {

            int chara = reader.read();
            if(chara != '%') {
                return SerializeResult.failure("Unable to parse placeholder! Expected placeholder to start with '%'");
            }

            StringBuilder placeholder = new StringBuilder();

            CharBuffer buffer = CharBuffer.allocate(1024);
            boolean param;

            while(true) {

                buffer.clear();

                reader.mark(1024);
                int bytesRead = reader.read(buffer);
                if(bytesRead == -1) {
                    return SerializeResult.failure("Unable to parse placeholder! Found EOF while parsing placeholder ID!");
                }

                String str = buffer.rewind().toString().substring(0, bytesRead);

                int endIndex = str.indexOf('%');
                int paramIndex = str.indexOf('<');

                if(endIndex == -1) endIndex = Integer.MAX_VALUE;
                if(paramIndex == -1) paramIndex = Integer.MAX_VALUE;

                int index = Math.min(endIndex, paramIndex);

                param = paramIndex < endIndex;

                if(index == Integer.MAX_VALUE) {
                    placeholder.append(str);
                } else {
                    reader.reset();
                    reader.skip(index + 1);
                    placeholder.append(str, 0, index);
                    break;
                }
            }
            buffer.clear();

            String finalId = placeholder.toString();

            if(param) {

                SerializeResult<UnresolvedComponent> res = UnresolvedComponent.parse(reader, '>', tryParseJSON);
                if(!res.isComplete()) {
                    return SerializeResult.failure("Unable to parse placeholder argument! " + res.get());
                }
                if(reader.read() != '%') {
                    return SerializeResult.failure("Unable to parse placeholder! Found junk data after argument!");
                }
                return SerializeResult.success(new UnresolvedPlaceholder(finalId, res.getOrThrow()));
            } else {
                return SerializeResult.success(new UnresolvedPlaceholder(finalId, null));
            }

        } catch (IOException ex) {

            return SerializeResult.failure("Unable to parse placeholder! Encountered IOException! " + ex.getMessage());
        }
    }
}
