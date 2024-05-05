package net.lyof.sortilege.enchants;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.enchants.armor.MagicProtectionEnchantment;
import net.lyof.sortilege.enchants.common.SoulboundEnchantment;
import net.lyof.sortilege.enchants.weapon.ArcaneEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModEnchants {
    public static void register() {}
    
    public static Enchantment register(String name, Enchantment enchant) {
        return Registry.register(Registries.ENCHANTMENT, Sortilege.makeID(name), enchant);
    }

/*
    // STAFF ENCHANTS
    public static Enchantment POTENCY = register("potency",
            new StaffEnchantment(Enchantment.Rarity.COMMON, 5));
    public static Enchantment STABILITY = register("stability",
            new StaffEnchantment(Enchantment.Rarity.COMMON, 5));
    public static Enchantment CHAINING = register("chaining",
            new StaffEnchantment(Enchantment.Rarity.UNCOMMON, 3));
    public static Enchantment WISDOM = register("wisdom",
            new StaffEnchantment(Enchantment.Rarity.RARE, 2));

    public static Enchantment PUSH = register("push",
            new StaffEnchantment(Enchantment.Rarity.UNCOMMON, 2,
                    null, (candidate) -> !candidate.getDescriptionId().equals("enchantment.sortilege.pull")));
    public static Enchantment PULL = register("pull",
            new StaffEnchantment(Enchantment.Rarity.UNCOMMON, 2,
                    null, (candidate) -> !candidate.getDescriptionId().equals("enchantment.sortilege.push")));


    public static Enchantment BRAZIER = register("brazier",
            new ElementalStaffEnchantment(Enchantment.Rarity.UNCOMMON, 2,
                    List.of(new Triple<>(1f, 0.7f, 0f), new Triple<>(1f, 1f, 0f), new Triple<>(1f, 0.85f, 0f)),
                    (target, level) -> target.setSecondsOnFire(level * 4)));
    public static Enchantment BLIZZARD = register("blizzard",
            new ElementalStaffEnchantment(Enchantment.Rarity.UNCOMMON, 2,
                    List.of(new Triple<>(0.7f, 0.7f, 1f), new Triple<>(0.8f, 0.9f, 1f)),
                    (target, level) -> target.setTicksFrozen(target.getTicksFrozen() + 150*level)));
    public static Enchantment BLAST = register("blast",
            new ElementalStaffEnchantment(Enchantment.Rarity.UNCOMMON, 2,
                    List.of(new Triple<>(0.5f, 0.25f, 0f), new Triple<>(0.8f, 0.2f, 0f), new Triple<>(1f, 0.4f, 0f)),
                    null));
    public static Enchantment BLITZ = register("blitz",
            new ElementalStaffEnchantment(Enchantment.Rarity.UNCOMMON, 2,
                    List.of(new Triple<>(1f, 1f, 0f), new Triple<>(1f, 1f, 0.5f), new Triple<>(1f, 1f, 0.75f)), (target, level) -> {
                target.setDeltaMovement(0, -1, 0);
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40 * level));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40 * level));
            }));

    public static Enchantment IGNORANCE_CURSE = register("ignorance_curse",
            new CurseStaffEnchantment(Enchantment.Rarity.RARE));
*/

    // EXTRA ENCHANTS
    public static Enchantment MAGIC_PROTECTION = register("magic_protection",
            new MagicProtectionEnchantment(Enchantment.Rarity.UNCOMMON));
    public static Enchantment ARCANE = register("arcane",
            new ArcaneEnchantment(Enchantment.Rarity.RARE));

    public static Enchantment SOULBOUND = register("soulbound",
            new SoulboundEnchantment());
}
