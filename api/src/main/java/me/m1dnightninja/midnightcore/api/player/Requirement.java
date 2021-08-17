package me.m1dnightninja.midnightcore.api.player;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.config.ConfigSerializer;
import me.m1dnightninja.midnightcore.api.module.lang.ILangModule;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;

import java.util.List;

public class Requirement {

    private final RequirementType type;
    private final String value;

    public Requirement(RequirementType type, String value) {
        this.type = type;
        this.value = value;
    }

    public boolean check(MPlayer player) {

        return executeCheck(player, type, value);
    }

    public RequirementType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    protected boolean executeCheck(MPlayer player, RequirementType req, String value) {
        ILangModule mod = MidnightCoreAPI.getInstance().getModule(ILangModule.class);
        String check = value;

        if (mod != null) {
            check = mod.applyPlaceholdersFlattened(check, player);
        }

        return req.check(player, this, check);
    }

    public static final ConfigSerializer<Requirement> SERIALIZER = new ConfigSerializer<Requirement>() {
        @Override
        public Requirement deserialize(ConfigSection section) {

            if(section.has("values", List.class)) {

                MidnightCoreAPI.getLogger().warn("Creating a multi requirement");
                return MultiRequirement.fromList(section.getListFiltered("values", Requirement.class));

            } else {

                MIdentifier typeId = MIdentifier.parseOrDefault(section.getString("type"), "midnightcore");
                RequirementType type = RequirementType.REQUIREMENT_TYPE_REGISTRY.get(typeId);
                if (type == null) {
                    MidnightCoreAPI.getLogger().warn("Warning: Requirement type " + typeId + " does not exist!");
                }

                String value = section.getString("value");

                return new Requirement(type, value);
            }
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
