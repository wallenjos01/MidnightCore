package org.wallentines.mcore.adapter;

import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ItemReflector<M, C> {

    private final Field handleField;
    private final Method copyMethod;
    private final Class<C> bukkitClass;

    public ItemReflector(Class<C> bukkitClass) {

        this.bukkitClass = bukkitClass;

        try {
            handleField = bukkitClass.getDeclaredField("handle");
            handleField.setAccessible(true);

            copyMethod = bukkitClass.getDeclaredMethod("asNMSCopy", ItemStack.class);

        } catch (NoSuchFieldException ex) {
            throw new IllegalArgumentException("Class " + bukkitClass + " does not contain a handle field!");
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException("Class " + bukkitClass + " does not contain an asNMSCopy field!");
        }
    }

    @SuppressWarnings("unchecked")
    public M getHandle(ItemStack itemStack) {

        try {
            M out = (M) handleField.get(itemStack);
            if(out == null) {
                return copy(itemStack);
            }
            return out;

        } catch (IllegalAccessException ex) {
            return copy(itemStack);
        }
    }

    @SuppressWarnings("unchecked")
    private M copy(ItemStack itemStack) {
        try {
            return (M) copyMethod.invoke(bukkitClass, itemStack);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalStateException("Unable to copy item stack!");
        }
    }


}
