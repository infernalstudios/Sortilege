package net.lyof.sortilege.configs;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModCommonConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;


    public static final ForgeConfigSpec.ConfigValue<Integer> STAFF_ATTACK_COOLDOWN;
    public static final ForgeConfigSpec.ConfigValue<Integer> STAFF_XP_COST;

    public static final ForgeConfigSpec.ConfigValue<Double> WITCH_HAT_DROP;

    public static final ForgeConfigSpec.ConfigValue<Integer> ENCHANT_LIMIT;

    public static final ForgeConfigSpec.ConfigValue<Boolean> MAGIC_PROTECTION_PROTECTION_COMPATIBLE;


    static {
        BUILDER.push("SortilegeConfigs");

        BUILDER.push("Staffs");
        STAFF_ATTACK_COOLDOWN = BUILDER.comment("Cooldown between Staff attacks, in ticks:")
                        .defineInRange("attackCooldown", 25, 0, Integer.MAX_VALUE);
        STAFF_XP_COST = BUILDER.comment("How much experience points should Staffs need and consume xp on use: (0 included)")
                        .defineInRange("XPCost", 0, 0, Integer.MAX_VALUE);
        BUILDER.pop();


        BUILDER.push("Enchantments");
        ENCHANT_LIMIT = BUILDER.comment("How many enchantments can an item have at maximum. Set to -1 to disable limiter")
                        .defineInRange("enchantmentLimit", 3, -1, Integer.MAX_VALUE);

        MAGIC_PROTECTION_PROTECTION_COMPATIBLE = BUILDER.comment("Should Magic Protection enchantment be compatible with regular protection")
                        .define("magicProtection_protectionCompatible", false);
        BUILDER.pop();


        WITCH_HAT_DROP = BUILDER.comment("Chance for Witches to drop their hat. 0 means they never do, 1 means they always do:")
                        .defineInRange("witchHatDropChance", 0.1f, 0f, 1f);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
