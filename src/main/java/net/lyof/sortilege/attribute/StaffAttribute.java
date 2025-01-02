package net.lyof.sortilege.attribute;

import net.minecraft.entity.attribute.ClampedEntityAttribute;

import java.util.UUID;

public class StaffAttribute extends ClampedEntityAttribute {
    private final UUID uuid;

    public StaffAttribute(String name, double def, double min, double max, UUID uuid) {
        super(name, def, min, max);
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return this.uuid;
    }
}
