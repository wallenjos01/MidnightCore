package org.wallentines.mcore;

import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.midnightlib.requirement.Requirement;
import org.wallentines.midnightlib.requirement.RequirementType;

import java.util.HashMap;

public class CooldownRequirement<T> implements RequirementType<T> {

    private final HashMap<Requirement<T>, HashMap<T, Long>> cooldowns = new HashMap<>();

    @Override
    public boolean check(T t, ConfigObject configObject, Requirement<T> requirement) {

        HashMap<T, Long> cds = cooldowns.computeIfAbsent(requirement, k -> new HashMap<>());

        int targetMs;
        try {
            targetMs = configObject.asNumber().intValue();
        } catch (NumberFormatException ex) {
            return true;
        }

        if(!cds.containsKey(t) || System.currentTimeMillis() - cds.get(t) > targetMs) {

            cds.put(t, System.currentTimeMillis());
            return true;

        } else {

            cds.remove(t);
            if(cds.isEmpty()) {
                cooldowns.remove(requirement);
            }

            return false;
        }

    }
}
