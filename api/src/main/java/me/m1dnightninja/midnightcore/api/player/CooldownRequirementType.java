package me.m1dnightninja.midnightcore.api.player;

import java.util.HashMap;

public class CooldownRequirementType implements RequirementType {

    HashMap<Requirement, HashMap<MPlayer, Long>> cooldowns = new HashMap<>();

    @Override
    public boolean check(MPlayer player, Requirement req, String value) {

        HashMap<MPlayer, Long> cds = cooldowns.computeIfAbsent(req, k -> new HashMap<>());

        int targetMs;
        try {
            targetMs = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return true;
        }

        if(!cds.containsKey(player) || System.currentTimeMillis() - cds.get(player) > targetMs) {

            cds.put(player, System.currentTimeMillis());
            return true;

        } else {
            return false;
        }

    }
}
