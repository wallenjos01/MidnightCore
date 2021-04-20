package me.m1dnightninja.midnightcore.common.module;

import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.module.ISavePointModule;

import java.util.HashMap;
import java.util.UUID;

public abstract class AbstractSavePointModule<T> implements ISavePointModule {

    protected static final MIdentifier ID = MIdentifier.create("midnightcore","save_point");

    private final HashMap<UUID, HashMap<String, T>> saves = new HashMap<>();

    @Override
    public MIdentifier getId() {
        return ID;
    }

    public void savePlayer(UUID u, String id) {
        HashMap<String, T> map = saves.computeIfAbsent(u, k -> new HashMap<>());

        if(map.containsKey(id)) {
            removeSavePoint(u, id);
        }

        T point = createSavePoint(u);
        map.put(id, point);
    }

    public void loadPlayer(UUID u, String id) {
        if(!saves.containsKey(u) || !saves.get(u).containsKey(id)) return;
        loadSavePoint(u, saves.get(u).get(id));
    }

    public void removeSavePoint(UUID u, String id) {
        if(!saves.containsKey(u)) return;
        saves.get(u).remove(id);
    }

    public abstract void resetPlayer(UUID u);

    protected abstract T createSavePoint(UUID u);
    protected abstract void loadSavePoint(UUID u, T point);

}
