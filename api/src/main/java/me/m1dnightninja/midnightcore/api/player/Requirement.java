package me.m1dnightninja.midnightcore.api.player;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.config.ConfigSerializer;
import me.m1dnightninja.midnightcore.api.module.lang.ILangModule;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;

public class Requirement {

    private final RequirementType type;
    private final String value;

    public Requirement(RequirementType type, String value) {
        this.type = type;
        this.value = value;
    }

    public boolean check(MPlayer player) {

        ILangModule mod = MidnightCoreAPI.getInstance().getModule(ILangModule.class);
        String check = value;

        if (mod != null) {
            check = mod.applyPlaceholdersFlattened(check, player);
        }

        return type.check(player, check);
    }

    public RequirementType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public static final ConfigSerializer<Requirement> SERIALIZER = new ConfigSerializer<Requirement>() {
        @Override
        public Requirement deserialize(ConfigSection section) {

            MIdentifier typeId = MIdentifier.parseOrDefault(section.getString("type"), "midnightcore");
            RequirementType type = RequirementType.REQUIREMENT_TYPE_REGISTRY.get(typeId);
            if(type == null) {
                MidnightCoreAPI.getLogger().warn("Warning: Requirement type " + typeId + " does not exist!");
            }

            String value = section.getString("value");

            return new Requirement(type, value);
        }

        @Override
        public ConfigSection serialize(Requirement object) {

            ConfigSection out = new ConfigSection();
            out.set("type", RequirementType.REQUIREMENT_TYPE_REGISTRY.getId(object.type));
            out.set("value", object.value);

            return out;
        }
    };
}
