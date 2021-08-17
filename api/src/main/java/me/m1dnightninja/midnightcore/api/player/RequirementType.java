package me.m1dnightninja.midnightcore.api.player;

import me.m1dnightninja.midnightcore.api.math.Region;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.registry.MRegistry;

public interface RequirementType {

    boolean check(MPlayer player, Requirement req, String value);

    MRegistry<RequirementType> REQUIREMENT_TYPE_REGISTRY = new MRegistry<>();

    static RequirementType register(String id, RequirementType act) {
        return REQUIREMENT_TYPE_REGISTRY.register(MIdentifier.parseOrDefault(id, "midnightcore"), act);
    }

    RequirementType PERMISSION = register("permission", (player, req, value) -> player.hasPermission(value));
    RequirementType WORLD = register("world", (player, req, value) -> player.getDimension().equals(MIdentifier.parseOrDefault(value)));
    RequirementType IN_REGION = register("region", (player, req, value) -> Region.SERIALIZER.deserialize(value).isWithin(player.getLocation()));

    RequirementType COOLDOWN = register("cooldown", new CooldownRequirementType());

}