package net.lyof.sortilege.attributes;

import net.lyof.sortilege.Sortilege;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;

public class ModAttributes {
    public static DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Sortilege.MOD_ID);

    public static void register(IEventBus eventbus) {
        ATTRIBUTES.register(eventbus);
    }


    public static final UUID DAMAGE_UUID = UUID.fromString("ac3c87a5-d511-4b9f-92b6-c1687a6ba99a");
    public static final UUID PIERCE_UUID = UUID.fromString("a5f02195-2a72-4403-b16b-2c0e733e2079");
    public static final UUID RANGE_UUID = UUID.fromString("dee9c854-8668-4450-8994-46107d39265e");


    public static final RegistryObject<Attribute> STAFF_DAMAGE = ATTRIBUTES.register("generic.staff_damage",
            () -> new StaffAttribute("attribute.name.generic.staff_damage", 0f, 0f, 512f, DAMAGE_UUID));
    public static final RegistryObject<Attribute> STAFF_PIERCE = ATTRIBUTES.register("generic.staff_pierce",
            () -> new StaffAttribute("attribute.name.generic.staff_pierce", 0f, 0f, 512f, PIERCE_UUID));
    public static final RegistryObject<Attribute> STAFF_RANGE = ATTRIBUTES.register("generic.staff_range",
            () -> new StaffAttribute("attribute.name.generic.staff_range", 0f, 0f, 512f, RANGE_UUID));
}
