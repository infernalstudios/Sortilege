package net.lyof.sortilege.enchants;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.enchants.armor.MagicProtectionEnchantment;
import net.lyof.sortilege.enchants.staff.CurseStaffEnchantment;
import net.lyof.sortilege.enchants.staff.ElementalStaffEnchantment;
import net.lyof.sortilege.enchants.staff.StaffEnchantment;
import net.lyof.sortilege.enchants.weapon.ArcaneEnchantment;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchants {
    public static void register(IEventBus eventbus) {
        ENCHANTMENTS.register(eventbus);
    }


    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Sortilege.MOD_ID);


    // STAFF ENCHANTS
    public static RegistryObject<Enchantment> POTENCY = ENCHANTMENTS.register("potency",
            () -> new StaffEnchantment(Enchantment.Rarity.COMMON, 5));
    public static RegistryObject<Enchantment> STABILITY = ENCHANTMENTS.register("stability",
            () -> new StaffEnchantment(Enchantment.Rarity.COMMON, 5));
    public static RegistryObject<Enchantment> CHAINING = ENCHANTMENTS.register("chaining",
            () -> new StaffEnchantment(Enchantment.Rarity.UNCOMMON, 3));
    public static RegistryObject<Enchantment> WISDOM = ENCHANTMENTS.register("wisdom",
            () -> new StaffEnchantment(Enchantment.Rarity.RARE, 2));

    public static RegistryObject<Enchantment> PUSH = ENCHANTMENTS.register("push",
            () -> new StaffEnchantment(Enchantment.Rarity.UNCOMMON, 2));
    public static RegistryObject<Enchantment> PULL = ENCHANTMENTS.register("pull",
            () -> new StaffEnchantment(Enchantment.Rarity.UNCOMMON, 2));


    public static RegistryObject<Enchantment> BRAZIER = ENCHANTMENTS.register("brazier",
            () -> new ElementalStaffEnchantment(Enchantment.Rarity.UNCOMMON, 2,
                    ParticleTypes.FLAME, (target, level) -> target.setSecondsOnFire(level * 4)));
    public static RegistryObject<Enchantment> BLIZZARD = ENCHANTMENTS.register("blizzard",
            () -> new ElementalStaffEnchantment(Enchantment.Rarity.UNCOMMON, 2,
                    ParticleTypes.SNOWFLAKE, (target, level) -> target.setTicksFrozen(target.getTicksFrozen() + 150*level)));
    public static RegistryObject<Enchantment> BLAST = ENCHANTMENTS.register("blast",
            () -> new ElementalStaffEnchantment(Enchantment.Rarity.UNCOMMON, 2,
                    ParticleTypes.CRIT, null));
    public static RegistryObject<Enchantment> BLITZ = ENCHANTMENTS.register("blitz",
            () -> new ElementalStaffEnchantment(Enchantment.Rarity.UNCOMMON, 2,
                    ParticleTypes.ELECTRIC_SPARK, (target, level) -> {
                target.setDeltaMovement(0, -1, 0);
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40 * level));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40 * level));
            }));
    //which particles should i use guys?

    public static RegistryObject<Enchantment> IGNORANCE_CURSE = ENCHANTMENTS.register("ignorance_curse",
            () -> new CurseStaffEnchantment(Enchantment.Rarity.RARE));


    // EXTRA ENCHANTS
    public static RegistryObject<Enchantment> MAGIC_PROTECTION = ENCHANTMENTS.register("magic_protection",
            () -> new MagicProtectionEnchantment(Enchantment.Rarity.UNCOMMON));
    public static RegistryObject<Enchantment> ARCANE = ENCHANTMENTS.register("arcane",
            () -> new ArcaneEnchantment(Enchantment.Rarity.RARE));
}
