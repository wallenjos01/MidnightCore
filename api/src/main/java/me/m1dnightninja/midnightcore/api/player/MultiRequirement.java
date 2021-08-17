package me.m1dnightninja.midnightcore.api.player;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class MultiRequirement extends Requirement {

    private final LinkedHashMap<RequirementType, String> requirements = new LinkedHashMap<>();

    public MultiRequirement(RequirementType type, String value) {
        super(type, value);

        addRequirement(type, value);
    }

    public void addRequirement(RequirementType type, String value) {
        requirements.put(type, value);
    }

    @Override
    public boolean check(MPlayer player) {

        for(Map.Entry<RequirementType, String> req : requirements.entrySet()) {

            if(!executeCheck(player, req.getKey(), req.getValue())) {
                return false;
            }
        }

        return true;
    }

    public static MultiRequirement fromList(Iterable<Requirement> requirements) {

        Iterator<Requirement> req = requirements.iterator();

        Requirement next = req.next();
        MultiRequirement out = new MultiRequirement(next.getType(), next.getValue());

        while(req.hasNext()) {
            next = req.next();
            out.addRequirement(next.getType(), next.getValue());
        }

        return out;
    }

}
