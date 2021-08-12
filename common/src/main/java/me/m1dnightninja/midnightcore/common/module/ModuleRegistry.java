package me.m1dnightninja.midnightcore.common.module;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.IModule;
import me.m1dnightninja.midnightcore.api.module.IModuleRegistry;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;

import java.util.*;

public class ModuleRegistry implements IModuleRegistry {

    private final List<IModule> loadedModules = new ArrayList<>();

    private final HashMap<MIdentifier, Integer> indicesById = new HashMap<>();
    private final HashMap<Class<? extends IModule>, Integer> indicesByClass = new HashMap<>();

    private final ConfigSection config;
    private final Set<MIdentifier> disabledModules = new HashSet<>();

    public ModuleRegistry(ConfigSection config) {
        this.config = config;

        disabledModules.addAll(config.getListFiltered("disabled_modules", MIdentifier.class));

    }

    @Override
    public IModule getModuleById(MIdentifier id) {

        Integer index = indicesById.get(id);
        if(index == null) return null;

        return loadedModules.get(index);
    }

    @Override
    public boolean isModuleLoaded(MIdentifier id) {

        return indicesById.containsKey(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IModule> T getModule(Class<T> clazz) {

        Integer index = indicesByClass.get(clazz);
        if(index != null) return (T) loadedModules.get(index);

        for(int i = 0 ; i < loadedModules.size() ; i++) {

            IModule mod = loadedModules.get(i);
            if(clazz.isAssignableFrom(mod.getClass())) {

                indicesByClass.put(clazz, i);
                return (T) mod;
            }
        }

        return null;
    }

    @Override
    public boolean loadModule(IModule module) {

        if(indicesById.containsKey(module.getId()) || indicesByClass.containsKey(module.getClass())) {
            MidnightCoreAPI.getLogger().warn("Attempt to load duplicate module!");
            return false;
        }

        Set<Class<? extends IModule>> dependencies = module.getDependencies();
        if(dependencies != null) {
            for(Class<? extends IModule> clazz : dependencies) {
                if(!indicesByClass.containsKey(clazz)) {
                    MidnightCoreAPI.getLogger().warn("Unable to load module " + module.getId() + "! Unknown dependency " + clazz.getCanonicalName());
                    return false;
                }
            }
        }

        ConfigSection sec = config.has(module.getId().toString(), ConfigSection.class) ? config.getSection(module.getId().toString()) : new ConfigSection();
        ConfigSection defaults = module.getDefaultConfig();

        if(defaults != null) {
            sec.fill(defaults);
            config.set(module.getId().toString(), sec);
        }

        int index = loadedModules.size();

        try {
            module.initialize(sec);
        } catch (Exception ex) {

            MidnightCoreAPI.getLogger().warn("An error occurred while trying to enable a module!");

            ex.printStackTrace();
            return false;
        }
        loadedModules.add(module);
        indicesById.put(module.getId(), index);
        indicesByClass.put(module.getClass(), index);

        return true;
    }

    @Override
    public void unloadModule(IModule module) {

        if(getModuleById(module.getId()) != module) return;

        int index = indicesById.get(module.getId());

        try {
            module.onDisable();
        } catch (Exception ex) {

            MidnightCoreAPI.getLogger().warn("An error occurred while trying to disable a module!");
            ex.printStackTrace();
        }

        indicesById.remove(module.getId());

        List<Class<? extends IModule>> unload = new ArrayList<>();
        for(HashMap.Entry<Class<? extends IModule>, Integer> ent : indicesByClass.entrySet()) {
            if(ent.getValue() == index) unload.add(ent.getKey());
        }

        for(Class<? extends IModule> clazz : unload) {
            indicesByClass.remove(clazz);
        }

        loadedModules.set(index, new EmptyModule());
    }

    @Override
    public Collection<MIdentifier> getLoadedModuleIds() {
        return indicesById.keySet();
    }


    public void loadAll(IModule... modules) {

        Queue<IModule> queue = new ArrayDeque<>(Arrays.asList(modules));

        while(!queue.isEmpty()) {
            loadWithDependencies(queue.remove(), queue);
        }
    }

    private boolean loadWithDependencies(IModule mod, Queue<IModule> other) {

        if(disabledModules.contains(mod.getId())) return false;

        Set<Class<? extends IModule>> dependencies = mod.getDependencies();
        if(dependencies == null) return loadModule(mod);

        for(Class<? extends IModule> clazz : dependencies) {

            if(getModule(clazz) != null) continue;

            boolean found = false;
            for(IModule dep : other) {

                if((dep.getClass() == clazz || clazz.isAssignableFrom(dep.getClass()))) {

                    other.remove(dep);
                    if(!loadWithDependencies(dep, other)) return false;

                    found = true;
                    break;
                }
            }

            if(!found) {
                return false;
            }
        }

        return loadModule(mod);
    }

    private static class EmptyModule implements IModule {

        @Override
        public boolean initialize(ConfigSection configuration) {
            return true;
        }

        @Override
        public MIdentifier getId() {
            return null;
        }

        @Override
        public ConfigSection getDefaultConfig() {
            return null;
        }
    }

}
