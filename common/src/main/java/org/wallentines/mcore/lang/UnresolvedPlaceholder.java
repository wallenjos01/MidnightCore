package org.wallentines.mcore.lang;

import org.wallentines.mcore.text.Component;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.midnightlib.types.Either;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * A data type which represents a placeholder which has been parsed but not resolved
 */
public class UnresolvedPlaceholder {

    private final PlaceholderSupplier supplier;
    private final String id;
    private final UnresolvedComponent argument;

    /**
     * Constructs a new unresolved placeholder
     * @param supplier The placeholder supplier
     * @param id The ID of the placeholder
     * @param argument The argument for the placeholder, if applicable
     */
    public UnresolvedPlaceholder(PlaceholderSupplier supplier, String id, UnresolvedComponent argument) {
        this.supplier = supplier;
        this.id = id;
        this.argument = argument;
    }

    /**
     * Returns the parsed placeholder supplier
     * @return The placeholder supplier
     */
    public PlaceholderSupplier getSupplier() {
        return supplier;
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
        if(supplier.acceptsArgument()) {
            out.append("<");
            if(argument != null) {
                out.append(argument);
            }
            out.append(">");
        }
        out.append("%");

        return out.toString();
    }

    /**
     * Resolves this placeholder according to the given context
     * @param ctx The context by which to resolve this placeholder
     * @return Either a String or a Component, depending on the placeholder supplier
     */
    public Either<String, Component> resolve(PlaceholderContext ctx) {

        if(argument != null) ctx.parameter = argument.resolve(ctx);
        return supplier.get(ctx);
    }

    static SerializeResult<UnresolvedPlaceholder> parse(BufferedReader reader, PlaceholderManager manager, boolean tryParseJSON) {

        try {

            int chara = reader.read();
            if(chara != '%') {
                return SerializeResult.failure("Unable to parse placeholder! Expected placeholder to start with '%'");
            }

            StringBuilder id = new StringBuilder();

            while ((chara = reader.read()) != -1) {

                if(chara == '%' || chara == '<') {
                    String finalId = id.toString();
                    PlaceholderSupplier supp = manager.getPlaceholderSupplier(finalId);

                    if(supp == null) {
                        return SerializeResult.failure("Unable to parse placeholder! Unable to find PlaceholderSupplier with name " + finalId + "!");
                    }

                    if(chara == '%') {
                        return SerializeResult.success(new UnresolvedPlaceholder(supp, finalId, null));
                    } else {
                        if(!supp.acceptsArgument()) {
                            return SerializeResult.failure("Unable to parse placeholder! Argument supplied for PlaceholderSupplier which does not accept one!");
                        }

                        SerializeResult<UnresolvedComponent> entry = UnresolvedComponent.parse(reader, '>', manager, tryParseJSON);
                        if(!entry.isComplete()) {
                            return SerializeResult.failure("Unable to parse placeholder argument! " + entry.get());
                        }
                        if(reader.read() != '%') {
                            return SerializeResult.failure("Unable to parse placeholder! Found junk data after argument!");
                        }
                        return SerializeResult.success(new UnresolvedPlaceholder(supp, finalId, entry.getOrThrow()));
                    }

                } else {
                    id.appendCodePoint(chara);
                }
            }

        } catch (IOException ex) {

            return SerializeResult.failure("Unable to parse placeholder! Encountered IOException! " + ex.getMessage());
        }

        return SerializeResult.failure("Unable to parse placeholder! An unknown error occurred!");
    }
}