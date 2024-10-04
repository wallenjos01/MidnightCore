package org.wallentines.mcore;

import org.wallentines.mdcfg.serializer.InlineSerializer;
import org.wallentines.mdcfg.serializer.Serializer;

public enum EquipmentSlot {

    MAINHAND("mainhand"),
    OFFHAND("offhand"),
    FEET("feet"),
    LEGS("legs"),
    CHEST("chest"),
    HEAD("head");

    private final String id;

    EquipmentSlot(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static EquipmentSlot byId(String id) {
        for(EquipmentSlot slot : values()) {
            if(slot.id.equals(id)) {
                return slot;
            }
        }
        return null;
    }

    public static final Serializer<EquipmentSlot> SERIALIZER = InlineSerializer.of(EquipmentSlot::getId, EquipmentSlot::byId);

}
