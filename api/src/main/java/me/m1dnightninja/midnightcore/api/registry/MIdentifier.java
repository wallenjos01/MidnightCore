package me.m1dnightninja.midnightcore.api.registry;

import me.m1dnightninja.midnightcore.api.config.InlineSerializer;

public class MIdentifier {

    private final String namespace;
    private final String path;

    private static final IllegalArgumentException EXCEPTION = new IllegalArgumentException("Unable to parse Module Identifier!");

    private MIdentifier(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPath() {
        return path;
    }


    public static MIdentifier create(String mod, String path) {

        return new MIdentifier(mod, path);
    }

    public static MIdentifier parse(String toParse) throws IllegalArgumentException {

        if(toParse == null) throw EXCEPTION;

        if(!toParse.contains(":")) {
            throw EXCEPTION;
        }

        String[] ss = toParse.split(":");
        if(ss.length > 2) {
            throw EXCEPTION;
        }

        return create(ss[0], ss[1]);
    }

    public static MIdentifier parseOrDefault(String toParse) {
        return parseOrDefault(toParse, "minecraft");
    }

    public static MIdentifier parseOrDefault(String toParse, String defaultNamespace) {

        if(toParse == null) throw EXCEPTION;

        String[] ss = toParse.split(":");

        return ss.length == 1 ? create(defaultNamespace, toParse) : create(ss[0], ss[1]);
    }

    @Override
    public boolean equals(Object obj) {

        if(obj instanceof MIdentifier) {

            MIdentifier mid = (MIdentifier) obj;
            return mid.namespace.equals(namespace) && mid.path.equals(path);

        } else {

            try {
                MIdentifier mid = parse(obj.toString());
                return equals(mid);

            } catch(IllegalArgumentException ex) {
                return false;
            }
        }
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public static final InlineSerializer<MIdentifier> SERIALIZER = new InlineSerializer<MIdentifier>() {
        @Override
        public MIdentifier deserialize(String s) {
            return parse(s);
        }

        @Override
        public String serialize(MIdentifier object) {
            return object.toString();
        }
    };

}
