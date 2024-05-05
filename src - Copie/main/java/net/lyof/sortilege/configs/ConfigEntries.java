package net.lyof.sortilege.configs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigEntries {
    public static void reload() {
        enchantLimiterDefault = new ConfigEntry<>("enchantments.enchant_limiter.default", 3).get();
        enchantLimiterMode = new ConfigEntry<>( "enchantments.enchant_limiter.override_mode", "relative").get();
        enchantLimiterOverrides = new ConfigEntry<Map<String, Double>>( "enchantments.enchant_limiter.overrides", new HashMap<>()).get();
        alwaysShowEnchantLimit = new ConfigEntry<>("enchantments.enchant_limiter.always_show_limit", true).get();

        isLimititeFoil = new ConfigEntry<>("enchantments.enchant_limiter.limitite.is_foil", true).get();
        doLimititeSpawn = new ConfigEntry<>("enchantments.enchant_limiter.limitite.generate_as_loot", true).get();
        maxLimitBreak = new ConfigEntry<>("enchantments.enchant_limiter.limitite.max_limit_break", 3).get();

        magicProtCompatibility = new ConfigEntry<>("enchantments.magic_protection_protection_compatibility", false).get();

        witchHatDropChance = new ConfigEntry<>("experience.witch_hat.drop_chance", 0.1).get();
        witchHatBonus = new ConfigEntry<>("experience.witch_hat.xp_bonus", 3).get();

        doIncreasedEnchantCosts = new ConfigEntry<>("experience.increased_enchant_costs", true).get();
        increasedEnchantCosts = new ConfigEntry<>("experience.costs", List.of(5d, 15d, 30d)).get();
        xpLevelCap = new ConfigEntry<>("experience.level_cap", 100).get();
        xpLinearCost = new ConfigEntry<>("experience.linear_xp_requirement", 40).get();

        bountyWhitelist = new ConfigEntry<>("experience.xp_bounty.tag_is_whitelist", false).get();
        bountyValue = new ConfigEntry<>("experience.xp_bounty.value", 20).get();
        bountyChance = new ConfigEntry<>("experience.xp_bounty.chance", 0.05).get();

        xpRequirements = new ConfigEntry<Map<String, Double>>( "experience.xp_requirements", new HashMap<>()).get();

        doXPKeep = new ConfigEntry<>("death.xp_keeping.enable", true).get();
        stealFromPlayers = new ConfigEntry<>("death.xp_keeping.allow_stealing_from_players", true).get();
        selfXPRatio = new ConfigEntry<>("death.xp_keeping.self_ratio", 0.3).get();
        attackerXPRatio = new ConfigEntry<>("death.xp_keeping.attacker_ratio", 0.6).get();
        dropXPRatio = new ConfigEntry<>("death.xp_keeping.drop_ratio", 0.1).get();

        keepEquipped = new ConfigEntry<>("death.keep_equipped", false).get();
        consumeSoulbound = new ConfigEntry<>("death.consume_soulbound", false).get();

        showDeathCoordinates = new ConfigEntry<>("death.show_coordinates_on_death", true).get();

        antidoteBlacklist = new ConfigEntry<List<String>>("brewing.antidote_blacklist", new ArrayList()).get();
        antidoteStackSize = new ConfigEntry<>("brewing.antidote_stack_size", 4).get();

        staffsDefaultCost = new ConfigEntry<>("staffs.default_xp_cost", 0).get();
        staffsDefaultCharge = new ConfigEntry<>("staffs.default_charge_time", 1).get();

        staffEntries = new ConfigEntry<List<Map<String, Map<String, Object>>>>("staffs.entries", new ArrayList()).get();
    }

    public static int enchantLimiterDefault;
    public static String enchantLimiterMode;
    public static Map<String, Double> enchantLimiterOverrides;
    public static boolean alwaysShowEnchantLimit;

    public static boolean isLimititeFoil;
    public static boolean doLimititeSpawn;
    public static int maxLimitBreak;

    public static boolean magicProtCompatibility;

    public static double witchHatDropChance;
    public static int witchHatBonus;

    public static boolean doIncreasedEnchantCosts;
    public static List<Double> increasedEnchantCosts;
    public static int xpLevelCap;
    public static int xpLinearCost;

    public static boolean bountyWhitelist;
    public static int bountyValue;
    public static double bountyChance;

    public static Map<String, Double> xpRequirements;

    public static boolean doXPKeep;
    public static boolean stealFromPlayers;
    public static double selfXPRatio;
    public static double attackerXPRatio;
    public static double dropXPRatio;

    public static boolean keepEquipped;
    public static boolean consumeSoulbound;

    public static boolean showDeathCoordinates;

    public static List<String> antidoteBlacklist;
    public static int antidoteStackSize;

    public static int staffsDefaultCost;
    public static int staffsDefaultCharge;

    public static List<Map<String, Map<String, Object>>> staffEntries;
}
