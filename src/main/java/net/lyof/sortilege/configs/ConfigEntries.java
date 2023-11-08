package net.lyof.sortilege.configs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigEntries {
    public static void reload() {
        EnchantLimiterDefault = new ConfigEntry<>("enchantments.enchant_limiter.default", 3).get();
        EnchantLimiterMode = new ConfigEntry<>( "enchantments.enchant_limiter.override_mode", "relative").get();
        AlwaysShowEnchantLimit = new ConfigEntry<>("enchantments.enchant_limiter.always_show_limit", true).get();
        DoLimititeSpawn = new ConfigEntry<>("enchantments.enchant_limiter.generate_limitite_loot", true).get();

        MagicProtCompatibility = new ConfigEntry<>("enchantments.magic_protection_protection_compatibility", false).get();

        WitchHatDropChance = new ConfigEntry<>("experience.witch_hat.drop_chance", 0.1).get();
        WitchHatBonus = new ConfigEntry<>("experience.witch_hat.xp_bonus", 3).get();

        DoIncreasedEnchantCosts = new ConfigEntry<>("experience.increased_enchant_costs", true).get();
        IncreasedEnchantCosts = new ConfigEntry<>("experience.costs", List.of(5d, 15d, 30d)).get();
        xpLevelCap = new ConfigEntry<>("experience.level_cap", 100).get();
        xpLinearCost = new ConfigEntry<>("experience.linear_xp_requirement", 40).get();

        BountyWhitelist = new ConfigEntry<>("death.xp_bounty.tag_is_whitelist", false).get();
        BountyValue = new ConfigEntry<>("death.xp_bounty.value", 20).get();
        BountyChance = new ConfigEntry<>("death.xp_bounty.chance", 0.05).get();

        DoXPKeep = new ConfigEntry<>("death.xp_keeping.enable", true).get();
        StealFromPlayers = new ConfigEntry<>("death.xp_keeping.allow_stealing_from_players", true).get();
        SelfXPRatio = new ConfigEntry<>("death.xp_keeping.self_ratio", 0.3).get();
        AttackerXPRatio = new ConfigEntry<>("death.xp_keeping.attacker_ratio", 0.6).get();
        DropXPRatio = new ConfigEntry<>("death.xp_keeping.drop_ratio", 0.1).get();

        ShowDeathCoordinates = new ConfigEntry<>("death.show_coordinates_on_death", true).get();

        StaffsHdParticles = new ConfigEntry<>("staffs.use_hd_particles", false).get();
        StaffsDefaultCost = new ConfigEntry<>("staffs.default_xp_cost", 0).get();
        StaffsDefaultCharge = new ConfigEntry<>("staffs.default_charge_time", 1).get();

        StaffEntries = new ConfigEntry<List<Map<String, Map<String, ?>>>>("staffs.entries", new ArrayList()).get();
    }

    public static int EnchantLimiterDefault;
    public static String EnchantLimiterMode;
    public static boolean AlwaysShowEnchantLimit;
    public static boolean DoLimititeSpawn;

    public static boolean MagicProtCompatibility;

    public static double WitchHatDropChance;
    public static int WitchHatBonus;

    public static boolean DoIncreasedEnchantCosts;
    public static List<Double> IncreasedEnchantCosts;
    public static int xpLevelCap;
    public static int xpLinearCost;

    public static boolean BountyWhitelist;
    public static int BountyValue;
    public static double BountyChance;

    public static boolean DoXPKeep;
    public static boolean StealFromPlayers;
    public static double SelfXPRatio;
    public static double AttackerXPRatio;
    public static double DropXPRatio;

    public static boolean ShowDeathCoordinates;

    public static boolean StaffsHdParticles;
    public static int StaffsDefaultCost;
    public static int StaffsDefaultCharge;

    public static List<Map<String, Map<String, ?>>> StaffEntries;
}
