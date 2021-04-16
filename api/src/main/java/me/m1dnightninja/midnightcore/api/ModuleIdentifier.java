package me.m1dnightninja.midnightcore.api;

public class ModuleIdentifier {

    private final String mod;
    private final String id;

    private static final IllegalArgumentException EXCEPTION = new IllegalArgumentException("Unable to parse Module Identifier!");

    private ModuleIdentifier(String mod, String id) {
        this.mod = mod;
        this.id = id;
    }

    public static ModuleIdentifier create(String mod, String id) {

        return new ModuleIdentifier(mod, id);
    }

    public static ModuleIdentifier parse(String toParse) throws IllegalArgumentException {

        if(!toParse.contains(":")) {
            throw EXCEPTION;
        }

        String[] ss = toParse.split(":");
        if(ss.length > 2) {
            throw EXCEPTION;
        }

        return create(ss[0], ss[1]);
    }

    @Override
    public boolean equals(Object obj) {

        if(obj instanceof ModuleIdentifier) {

            ModuleIdentifier mid = (ModuleIdentifier) obj;

            return mid.mod.equals(mod) && mid.id.equals(id);

        } else {

            try {
                ModuleIdentifier mid = parse(obj.toString());
                return equals(mid);

            } catch(IllegalArgumentException ex) {
                return false;
            }
        }
    }

    @Override
    public String toString() {
        return mod + ":" + id;
    }
}
