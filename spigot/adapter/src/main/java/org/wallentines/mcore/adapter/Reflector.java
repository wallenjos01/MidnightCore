package org.wallentines.mcore.adapter;

import java.lang.reflect.Field;

public class Reflector<M,C> {

    private final Field handleField;

    public Reflector(Class<C> bukkitClass, String className, String field) {
        try {
            Class<?> clazz = Class.forName(className);
            handleField = clazz.getDeclaredField(field);
            handleField.setAccessible(true);
        } catch (NoSuchFieldException | ClassNotFoundException ex) {
            throw new IllegalArgumentException("Class " + bukkitClass + " does not contain a field called " + field + "!");
        }
    }

    @SuppressWarnings("unchecked")
    public M getHandle(C objective) {

        try {
            return (M) handleField.get(objective);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Unable to get objective handle!");
        }
    }
}
