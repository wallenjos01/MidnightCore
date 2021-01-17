package me.m1dnightninja.midnightcore.spigot.util;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import org.bukkit.Bukkit;

import java.lang.reflect.*;

public final class ReflectionUtil {

    public static final String API_VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".",",").split(",")[3];

    public static Class<?> getNMSClass(String name) {
        try {
            return Class.forName(String.format("net.minecraft.server.%s.%s", API_VERSION, name));
        } catch (ClassNotFoundException e) {
            MidnightCoreAPI.getLogger().warn("Unable to load NMS class " + name + "!");
            throw new IllegalStateException();
        }
    }

    public static Class<?> getCraftBukkitClass(String name) {
        try {
            return Class.forName(String.format("org.bukkit.craftbukkit.%s.%s", API_VERSION, name));
        } catch (ClassNotFoundException e) {
            MidnightCoreAPI.getLogger().warn("Unable to load CraftBukkit class " + name + "!");
            throw new IllegalStateException();
        }
    }

    public static Field getFieldByType(Class<?> clazz, Class<?> type) {
        return getFieldByType(clazz, type, 0);
    }

    public static Field getFieldByType(Class<?> clazz, Class<?> type, int index) {
        int found = 0;
        for(Field f : clazz.getDeclaredFields()) {
            if(f.getType() == type) {
                if(found == index) {
                    return f;
                }
                found++;
            }
        }
        MidnightCoreAPI.getLogger().warn("Unable to find field with type " + type.getName() + " in class " + clazz.getName());
        throw new IllegalStateException();
    }

    public static Field getField(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            MidnightCoreAPI.getLogger().warn("Unable to find field " + name + " in class " + clazz.getName() + "!");
            throw new IllegalStateException();
        }
    }

    public static Object getFieldValue(Object o, Field f, boolean force) {
        try {
            if(force) f.setAccessible(true);
            return f.get(o);
        } catch(IllegalAccessException ex) {
            MidnightCoreAPI.getLogger().warn("Unable to access field " + f.getName() + " in class " + o.getClass().getName() + "!");
            throw new IllegalStateException();
        }
    }

    public static boolean setFieldValue(Object o, Field f, Object value, boolean force) {
        try {
            if(force) f.setAccessible(true);
            f.set(o, value);
            return true;
        } catch (IllegalAccessException ex) {
            MidnightCoreAPI.getLogger().warn("Unable to access field " + f.getName() + " in class " + o.getClass().getName() + "!");
            return false;
        }
    }

    public static Method getMethodByReturnType(Class<?> clazz, Class<?> ret, Class<?>... args) {
        for(Method m : clazz.getDeclaredMethods()) {
            if(m.getReturnType() == ret && m.getParameterCount() == args.length) {
                if(m.getParameterCount() == 0) {
                    return m;
                }
                boolean broke = false;
                for(int i = 0 ; i < m.getParameterCount() ; i++) {
                    if(args[i] != m.getParameterTypes()[i]) {
                        broke = true;
                        break;
                    }
                }
                if(!broke) return m;
            }
        }
        MidnightCoreAPI.getLogger().warn("Unable to find method with return type " + ret.getName() + " in class " + clazz.getName() + "!");
        throw new IllegalStateException();
    }

    public static Method getMethod(Class<?> clazz, String name, Class<?>... args) {
        try {
            return clazz.getDeclaredMethod(name, args);
        } catch (NoSuchMethodException e) {
            MidnightCoreAPI.getLogger().warn("Unable to find method " + name + " in class " + clazz.getName() + "!");
            throw new IllegalStateException();
        }
    }

    public static Object callMethod(Object o, Method m, boolean force, Object... args) {
        try {
            if(force) m.setAccessible(true);
            return m.invoke(o, args);
        } catch (InvocationTargetException | IllegalAccessException e) {
            MidnightCoreAPI.getLogger().warn("Unable to access method " + m.getName() + " in class " + o.getClass().getName() + "!");
            throw new IllegalStateException();
        }
    }

    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... args) {
        try {
            return clazz.getDeclaredConstructor(args);
        } catch (NoSuchMethodException e) {
            MidnightCoreAPI.getLogger().warn("Unable to find constructor in class " + clazz.getName() + "!");

            for(Constructor<?> cons : clazz.getDeclaredConstructors()) {

                StringBuilder builder = new StringBuilder(cons.getName()).append(", ");
                for(Class<?> clz : cons.getParameterTypes()) {
                    builder.append(clz.getName()).append(", ");
                }

                MidnightCoreAPI.getLogger().info(builder.toString());
            }

            throw new IllegalStateException();
        }
    }

    public static <T> T construct(Constructor<T> cons, Object... args) {
        try {
            return cons.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            MidnightCoreAPI.getLogger().warn("Unable to construct new " + cons.getDeclaringClass().getName() + "!");
            throw new IllegalStateException();
        }
    }

    public static <T> T castTo(Object o, Class<T> clazz) {
        try {
            return clazz.cast(o);
        } catch(ClassCastException ex) {
            MidnightCoreAPI.getLogger().warn("Unable to cast object with class " + o.getClass().getName() + " to " + clazz.getName() + "!");
            throw new IllegalStateException();
        }
    }

    public static Object getEnumValue(Class<?> clazz, String name) {
        if(clazz.isEnum()) {
            for (Object o : clazz.getEnumConstants()) {
                if (o.toString().equals(name)) {
                    return o;
                }
            }
        }
        throw new IllegalStateException();
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getArrayClass(Class<T> clazz) {
        return (Class<T>) Array.newInstance(clazz, 0).getClass();
    }
}
