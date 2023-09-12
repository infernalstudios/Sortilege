package net.lyof.sortilege.attributes;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.UUID;

public class StaffAttribute extends RangedAttribute {
    public UUID uuid;

    public StaffAttribute(String name, double def, double min, double max, UUID uuid) {
        super(name, def, min, max);
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return this.uuid;
    }
}
