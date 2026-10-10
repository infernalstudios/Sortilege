package net.lyof.sortilege.attribute;

import net.lcc.sollib.api.common.registry.SHolder;
import net.lcc.sollib.api.common.registry.SolModContainer;
import net.lyof.sortilege.Sortilege;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModAttributes {
    public static class AttributeHolder extends SHolder<Attribute> {
        public AttributeHolder(SolModContainer mod, String name, Supplier<Attribute> entrySupplier) {
            super(mod, name, entrySupplier);
        }

        @Override
        public Registry<Attribute> getRegistry() {
            return BuiltInRegistries.ATTRIBUTE;
        }
    }

    public static final List<AttributeHolder> GLOBALS = new ArrayList<>();

    public static void register() {}

    public static AttributeHolder register(String name, boolean global, Supplier<Attribute> attribute) {
        AttributeHolder holder = Sortilege.MOD.register(AttributeHolder.class, name, attribute);
        if (global) GLOBALS.add(holder);
        return holder;
    }

    public static final AttributeHolder STAFF_DAMAGE = register("staff.damage", true,
            () -> new RangedAttribute("attribute.sortilege.name.staff_damage", 0f, 0f, 512f));
    public static final AttributeHolder STAFF_PIERCE = register("staff.pierce", true,
            () ->  new RangedAttribute("attribute.sortilege.name.staff_pierce", 0f, 0f, 512f));
    public static final AttributeHolder STAFF_RANGE = register("staff.range", true,
            () -> new RangedAttribute("attribute.sortilege.name.staff_range", 0f, 0f, 512f));
}
