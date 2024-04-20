package org.wallentines.mcore;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Arrays;
import java.util.Collection;

public class IdentifierArgument implements ArgumentType<Identifier> {

    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");

    private static boolean isValid(char c) {

        return c >= '0' && c <= '9'
                || c >= 'a' && c <= 'z'
                || c == '_' || c == ':'
                || c == '/' || c == '.'
                || c == '-';
    }

    private final String namespace;

    public IdentifierArgument(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public Identifier parse(StringReader reader) throws CommandSyntaxException {

        int i = reader.getCursor();

        while(reader.canRead() && isValid(reader.peek())) {
            reader.skip();
        }

        String out = reader.getString().substring(i, reader.getCursor());
        return Identifier.parseOrDefault(out, namespace);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    static final IdentifierArgument MCORE = new IdentifierArgument(MidnightCoreAPI.MOD_ID);

}
