package net.lyof.sortilege.attribute;

import net.lyof.sortilege.Sortilege;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.UUID;

public class ModAttributes {
    public static void register() {}

    public static StaffAttribute register(String name, StaffAttribute attribute) {
        attribute.setTracked(true);
        return Registry.register(Registries.ATTRIBUTE, Sortilege.makeID(name), attribute);
    }


    private static final UUID DAMAGE_UUID = UUID.fromString("ac3c87a5-d511-4b9f-92b6-c1687a6ba99a");
    private static final UUID PIERCE_UUID = UUID.fromString("a5f02195-2a72-4403-b16b-2c0e733e2079");
    private static final UUID RANGE_UUID = UUID.fromString("dee9c854-8668-4450-8994-46107d39265e");


    public static final StaffAttribute STAFF_DAMAGE = register("generic.staff_damage",
            new StaffAttribute("attribute.name.generic.staff_damage", 0f, 0f, 512f, DAMAGE_UUID));
    public static final StaffAttribute STAFF_PIERCE = register("generic.staff_pierce",
            new StaffAttribute("attribute.name.generic.staff_pierce", 0f, 0f, 512f, PIERCE_UUID));
    public static final StaffAttribute STAFF_RANGE = register("generic.staff_range",
            new StaffAttribute("attribute.name.generic.staff_range", 0f, 0f, 512f, RANGE_UUID));
}
