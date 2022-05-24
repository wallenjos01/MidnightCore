package org.wallentines.midnightcore.api.requirement;

import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.requirement.Requirement;
import org.wallentines.midnightlib.requirement.RequirementType;

import java.util.HashMap;

public class CooldownRequirementType implements RequirementType<MPlayer> {

    HashMap<Requirement<MPlayer>, HashMap<MPlayer, Long>> cooldowns = new HashMap<>();

    @Override
    public boolean check(MPlayer player, Requirement<MPlayer> req, String value) {

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
