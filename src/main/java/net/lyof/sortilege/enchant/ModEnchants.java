package net.lyof.sortilege.enchant;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.enchant.armor.MagicProtectionEnchantment;
import net.lyof.sortilege.enchant.common.CurseEnchantment;
import net.lyof.sortilege.enchant.common.SoulboundEnchantment;
import net.lyof.sortilege.enchant.staff.CurseStaffEnchantment;
import net.lyof.sortilege.enchant.staff.ElementalStaffEnchantment;
import net.lyof.sortilege.enchant.staff.StaffEnchantment;
import net.lyof.sortilege.enchant.weapon.ArcaneEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.List;

public class ModEnchants {
    public static void register() {}
    
    public static Enchantment register(String name, Enchantment enchant) {
        if (!isEnabled(name)) return null;
        return Registry.register(Registries.ENCHANTMENT, Sortilege.makeID(name), enchant);
    }

    private static boolean isEnabled(String name) {
        return ConfigEntries.enabledEnchants.getOrDefault(name, true);
    }


    // STAFF ENCHANTS
    public static final Enchantment POTENCY = register("potency",
            new StaffEnchantment(Enchantment.Rarity.COMMON, 5));
    public static final Enchantment STABILITY = register("stability",
            new StaffEnchantment(Enchantment.Rarity.COMMON, 5));
    public static final Enchantment CHAINING = register("chaining",
            new StaffEnchantment(Enchantment.Rarity.COMMON, 3));
    public static final Enchantment WISDOM = register("wisdom",
            new StaffEnchantment(Enchantment.Rarity.UNCOMMON, 3,
                    null, candidate -> !candidate.getTranslationKey().equals("enchantment.sortilege.focus")));

    public static final Enchantment PUSH = register("push",
            new StaffEnchantment(Enchantment.Rarity.UNCOMMON, 2,
                    null, candidate -> !candidate.getTranslationKey().equals("enchantment.sortilege.pull")));
    public static final Enchantment PULL = register("pull",
            new StaffEnchantment(Enchantment.Rarity.UNCOMMON, 2,
                    null, candidate -> !candidate.getTranslationKey().equals("enchantment.sortilege.push")));

    public static final Enchantment FOCUS = register("focus",
            new StaffEnchantment(Enchantment.Rarity.UNCOMMON, 5,
                    null, candidate -> !candidate.getTranslationKey().equals("enchantment.sortilege.wisdom")));


    public static final Enchantment BRAZIER = register("brazier",
            new ElementalStaffEnchantment(Enchantment.Rarity.UNCOMMON, 2,
                    List.of(new float[]{1f, 0.7f, 0f},
                            new float[]{1f, 1f, 0f},
                            new float[]{1f, 0.85f, 0f}),
                    (target, level) -> target.setOnFireFor(level * 4)));
    public static final Enchantment BLIZZARD = register("blizzard",
            new ElementalStaffEnchantment(Enchantment.Rarity.UNCOMMON, 2,
                    List.of(new float[]{0.7f, 0.7f, 1f},
                            new float[]{0.8f, 0.9f, 1f}),
                    (target, level) -> {
                if (target.isOnFire()) {
                    target.extinguish();
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 40, level));
                }
                target.setFrozenTicks(target.getFrozenTicks() + 160*level);
            }));
    public static final Enchantment BLAST = register("blast",
            new ElementalStaffEnchantment(Enchantment.Rarity.UNCOMMON, 2,
                    List.of(new float[]{0.5f, 0.25f, 0f},
                            new float[]{0.8f, 0.2f, 0f},
                            new float[]{1f, 0.4f, 0f}),
                    null));
    public static final Enchantment BLITZ = register("blitz",
            new ElementalStaffEnchantment(Enchantment.Rarity.UNCOMMON, 2,
                    List.of(new float[]{1f, 1f, 0f},
                            new float[]{1f, 1f, 0.5f},
                            new float[]{1f, 1f, 0.75f}),
                    (target, level) -> {
                target.setVelocity(0, -1, 0);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 40 * level, 0));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40 * level, 1));
            }));
    public static final Enchantment BLESSING = register("blessing",
            new ElementalStaffEnchantment(Enchantment.Rarity.UNCOMMON, 2,
                    List.of(new float[]{1f, 0.75f, 0.75f},
                            new float[]{1f, 0.5f, 0.5f},
                            new float[]{1f, 0.25f, 0.25f}),
                    null));

    public static final Enchantment BONK = register("bonk",
            new StaffEnchantment(Enchantment.Rarity.RARE, 1));

    public static final Enchantment IGNORANCE_CURSE = register("ignorance_curse",
            new CurseStaffEnchantment(Enchantment.Rarity.RARE));


    // EXTRA ENCHANTS
    public static final Enchantment MAGIC_PROTECTION = register("magic_protection",
            new MagicProtectionEnchantment(Enchantment.Rarity.COMMON));
    public static final Enchantment ARCANE = register("arcane",
            new ArcaneEnchantment(Enchantment.Rarity.UNCOMMON));

    public static final Enchantment SOULBOUND = register("soulbound",
            new SoulboundEnchantment());
    public static final Enchantment STORYTELLING_CURSE = register("storytelling_curse",
            new CurseEnchantment(Enchantment.Rarity.RARE, EnchantmentTarget.VANISHABLE, EquipmentSlot.values()));
}
