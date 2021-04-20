package me.m1dnightninja.midnightcore.api.registry;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;

import java.util.ArrayList;
import java.util.HashMap;

public class MRegistry<T> {

    private final ArrayList<MIdentifier> ids = new ArrayList<>();
    private final ArrayList<T> values = new ArrayList<>();

    private final HashMap<MIdentifier, Integer> indexById = new HashMap<>();
    private final HashMap<T, Integer> indexByValue = new HashMap<>();

    private int size = 0;

    public T register(MIdentifier id, T value) {

        if(ids.contains(id)) {
            throw new IllegalArgumentException("Attempt to register item with duplicate ID!");
        }
        if(values.contains(value)) {
            throw new IllegalArgumentException("Attempt to register item twice!");
        }

        ids.add(id);
        values.add(value);

        indexById.put(id, size);
        indexByValue.put(value, size);

        size++;

        return value;
    }

    public T get(MIdentifier id) {

        if(id == null) {
            throw new IllegalArgumentException("ID must not be null!");
        }

        Integer index = indexById.get(id);

        if(index == null || index < 0) {
            throw new IllegalStateException("Attempt to get an unregistered item!");
        }

        return values.get(index);
    }

    public MIdentifier getId(T value) {

        if(value == null) {
            throw new IllegalArgumentException("Value must not be null!");
        }

        Integer index = indexByValue.get(value);

        if(index == null || index < 0) {
            throw new IllegalStateException("Attempt to get ID of unregistered item!");
        }

        return ids.get(index);
    }

    public T valueAtIndex(int index) {

        if(index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for registry of size " + size + "!");
        }

        return values.get(index);
    }

    public MIdentifier idAtIndex(int index) {

        if(index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for registry of size " + size + "!");
        }

        return ids.get(index);
    }

    public void clear() {

        values.clear();
        ids.clear();

        indexById.clear();
        indexByValue.clear();

        size = 0;
    }

    public T remove(T value) {

        Integer index = indexByValue.get(value);

        if(index == null || index < 0) {
            throw new IllegalStateException("Attempt to remove unregistered item!");
        }

        return removeAtIndex(index);
    }

    public T removeById(MIdentifier id) {

        Integer index = indexById.get(id);

        if(index == null || index < 0) {
            throw new IllegalStateException("Attempt to remove item with unregistered ID!");
        }

        return removeAtIndex(index);
    }

    public T removeAtIndex(int index) {

        if(index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for registry of size " + size + "!");
        }

        T out = values.remove(index);
        ids.remove(index);

        size--;

        for(int i = index ; i < size ; i++) {
            indexById.put(ids.get(i), i);
            indexByValue.put(values.get(i), i);
        }

        return out;
    }

    public int getSize() {
        return size;
    }


    public boolean isRegistered(T value) {
        return indexByValue.containsKey(value);
    }

    public boolean contains(MIdentifier id) {
        return indexById.containsKey(id);
    }

    public ConfigSection save() {

        ConfigSection out = new ConfigSection();

        for(int i = 0 ; i < size ; i++) {

            out.set(ids.get(i).toString(), values.get(i));
        }

        return out;
    }

    public void load(ConfigSection sec, Class<T> convertTo) {

        for(String key : sec.getKeys()) {

            MIdentifier id;

            try {
                id = MIdentifier.parse(key);
            } catch(IllegalArgumentException ex) {
                ex.printStackTrace();
                continue;
            }

            T obj = sec.get(key, convertTo);
            register(id, obj);
        }
    }

}
