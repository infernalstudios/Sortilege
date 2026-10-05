package net.lyof.sortilege.attribute;

import net.lyof.sortilege.Sortilege;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.ArrayList;
import java.util.List;

public class ModAttributes {
    public static final List<Holder<Attribute>> GLOBALS = new ArrayList<>();

    public static void register() {}

    public static Holder<Attribute> register(String name, boolean global, RangedAttribute attribute) {
        Holder<Attribute> holder = Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, Sortilege.MOD.makeID(name), attribute.setSyncable(true));
        if (global) GLOBALS.add(holder);
        return holder;
    }

    public static final Holder<Attribute> STAFF_DAMAGE = register("staff.damage", true,
            new RangedAttribute("attribute.sortilege.name.staff_damage", 0f, 0f, 512f));
    public static final Holder<Attribute> STAFF_PIERCE = register("staff.pierce", true,
            new RangedAttribute("attribute.sortilege.name.staff_pierce", 0f, 0f, 512f));
    public static final Holder<Attribute> STAFF_RANGE = register("staff.range", true,
            new RangedAttribute("attribute.sortilege.name.staff_range", 0f, 0f, 512f));
}
