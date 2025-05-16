package net.lyof.sortilege.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigEntries {
    public static void reload() {
        enchantLimiterDefault = new ConfigEntry<>("enchantments.enchant_limiter.default", 3).get();
        cursesAddSlots = new ConfigEntry<>("enchantments.enchant_limiter.curses_add_slots", true).get();
        enchantLimiterMode = new ConfigEntry<>( "enchantments.enchant_limiter.override_mode", "relative").get();
        enchantLimiterOverrides = new ConfigEntry<Map<String, Double>>( "enchantments.enchant_limiter.overrides", Map.of()).get();
        alwaysShowEnchantLimit = new ConfigEntry<>("enchantments.enchant_limiter.always_show_limit", true).get();

        isLimititeFoil = new ConfigEntry<>("enchantments.enchant_limiter.limitite.is_foil", true).get();
        limititeLootWeight = new ConfigEntry<>("enchantments.enchant_limiter.limitite.loot_weight", 24).get();
        maxLimitBreak = new ConfigEntry<>("enchantments.enchant_limiter.limitite.max_limit_break", 3).get();

        bookCatalysts = new ConfigEntry<>("enchantments.enchant_catalyst.allow_books", true).get();
        catalystChance = new ConfigEntry<>("enchantments.enchant_catalyst.catalyst_activation_chance", 0.5d).get();
        catalystTooltip = new ConfigEntry<>("enchantments.enchant_catalyst.show_in_tooltip", true).get();

        allowInventoryEnchanting = new ConfigEntry<>("enchantments.allow_inventory_enchanting", false).get();

        magicProtCompatibility = new ConfigEntry<>("enchantments.magic_protection_protection_compatibility", false).get();
        betterFeatherFalling = new ConfigEntry<>("enchantments.better_feather_falling", 4).get();
        betterUnbreaking = new ConfigEntry<>("enchantments.better_unbreaking", 3).get();
        betterMagicProt = new ConfigEntry<>("enchantments.better_magic_protection", true).get();
        betterFireProt = new ConfigEntry<>("enchantments.better_fire_protection", 4).get();
        betterBane = new ConfigEntry<>("enchantments.better_bane_of_arthropods", true).get();

        enabledEnchants = new ConfigEntry<Map<String, Boolean>>("enchantments.enabled_enchants", Map.of()).get();

        witchHatEnabled = new ConfigEntry<>("experience.witch_hat.enable", true).get();
        witchHatDropChance = new ConfigEntry<>("experience.witch_hat.drop_chance", 0.1).get();
        witchHatBonus = new ConfigEntry<>("experience.witch_hat.xp_bonus", 3).get();

        doIncreasedEnchantCosts = new ConfigEntry<>("experience.increased_enchant_costs", true).get();
        increasedEnchantCosts = new ConfigEntry<>("experience.costs", List.of(1d, 3d, 7d)).get();
        increasedEnchantNeeds = new ConfigEntry<>("experience.needed", List.of(5d, 15d, 30d)).get();

        noXPAnvil = new ConfigEntry<>("experience.no_xp_anvil", true).get();

        xpLevelCap = new ConfigEntry<>("experience.level_cap", 100).get();
        xpLinearCost = new ConfigEntry<>("experience.linear_xp_requirement", 40).get();

        bountyWhitelist = new ConfigEntry<>("experience.xp_bounty.tag_is_whitelist", false).get();
        bountyValue = new ConfigEntry<>("experience.xp_bounty.value", 20).get();
        bountyChance = new ConfigEntry<>("experience.xp_bounty.chance", 0.05).get();

        xpRequirements = new ConfigEntry<Map<String, Object>>( "experience.recipe_locks", Map.of()).get();

        doXPKeep = new ConfigEntry<>("death.xp_keeping.enable", true).get();
        stealFromPlayers = new ConfigEntry<>("death.xp_keeping.allow_stealing_from_players", true).get();
        selfXPRatio = new ConfigEntry<>("death.xp_keeping.self_ratio", 0.3).get();
        attackerXPRatio = new ConfigEntry<>("death.xp_keeping.attacker_ratio", 0.6).get();
        dropXPRatio = new ConfigEntry<>("death.xp_keeping.drop_ratio", 0.1).get();

        stolenXpData = new ConfigEntry<>("death.xp_keeping.stolen_xp_data", 41).get();

        keepEquipped = new ConfigEntry<>("death.keep_equipped", false).get();
        consumeSoulbound = new ConfigEntry<>("death.consume_soulbound", false).get();

        showDeathCoordinates = new ConfigEntry<>("death.show_coordinates_on_death", true).get();
        glowingKiller = new ConfigEntry<>("death.glowing_killer", true).get();

        antidoteEnabled = new ConfigEntry<>("brewing.antidote.enable", true).get();
        antidoteBlacklist = new ConfigEntry<List<String>>("brewing.antidote.effect_blacklist", List.of()).get();
        antidoteStackSize = new ConfigEntry<>("brewing.antidote.stack_size", 4).get();

        cauldronBrewingEnabled = new ConfigEntry<>("brewing.cauldron.enable", true).get();
        cauldronBlazeRefill = new ConfigEntry<>("brewing.cauldron.blaze_powder_refill", true).get();
        fillSwampHutCauldrons = new ConfigEntry<>("brewing.cauldron.fill_swamp_huts_randomly", true).get();
        swampHutBlacklist = new ConfigEntry<List<String>>("brewing.cauldron.swamp_hut_blacklist", List.of()).get();

        maxOvercharge = new ConfigEntry<>("staffs.overcharge.max_overcharge", 20).get();
        overchargeColor = new ConfigEntry<>("staffs.overcharge.bar_color", "#0000ff").get();
        overchargePreventsDurability = new ConfigEntry<>("staffs.overcharge.free_durability", true).get();
        overchargePreventsExperience = new ConfigEntry<>("staffs.overcharge.free_experience", true).get();
        overchargeIngredients = new ConfigEntry<Map<String, Double>>("staffs.overcharge.ingredients", Map.of()).get();

        staffsDefaultCost = new ConfigEntry<>("staffs.default_xp_cost", 0).get();
        staffsDefaultCharge = new ConfigEntry<>("staffs.default_charge_time", 1).get();

        staffEntries = new ConfigEntry<List<Map<String, Map<String, Object>>>>("staffs.entries", List.of()).get();
    }

    public static int enchantLimiterDefault;
    public static boolean cursesAddSlots;
    public static String enchantLimiterMode = "";
    public static Map<String, Double> enchantLimiterOverrides = Map.of();
    public static boolean alwaysShowEnchantLimit;

    public static boolean isLimititeFoil;
    public static int limititeLootWeight;
    public static int maxLimitBreak;

    public static boolean bookCatalysts;
    public static double catalystChance;
    public static boolean catalystTooltip;

    public static boolean allowInventoryEnchanting;

    public static boolean magicProtCompatibility;
    public static int betterFeatherFalling;
    public static int betterUnbreaking;
    public static boolean betterMagicProt;
    public static int betterFireProt;
    public static boolean betterBane;

    public static Map<String, Boolean> enabledEnchants = Map.of();

    public static boolean witchHatEnabled;
    public static double witchHatDropChance;
    public static int witchHatBonus;

    public static boolean doIncreasedEnchantCosts;
    public static List<Double> increasedEnchantCosts = List.of();
    public static List<Double> increasedEnchantNeeds = List.of();
    public static boolean noXPAnvil;
    public static int xpLevelCap;
    public static int xpLinearCost;

    public static boolean bountyWhitelist;
    public static int bountyValue;
    public static double bountyChance;

    public static Map<String, Object> xpRequirements = Map.of();

    public static boolean doXPKeep;
    public static boolean stealFromPlayers;
    public static double selfXPRatio;
    public static double attackerXPRatio;
    public static double dropXPRatio;
    public static int stolenXpData;

    public static boolean keepEquipped;
    public static boolean consumeSoulbound;

    public static boolean showDeathCoordinates;
    public static boolean glowingKiller;

    public static boolean antidoteEnabled;
    public static List<String> antidoteBlacklist = List.of();
    public static int antidoteStackSize;

    public static boolean cauldronBrewingEnabled;
    public static boolean cauldronBlazeRefill;
    public static boolean fillSwampHutCauldrons;
    public static List<String> swampHutBlacklist = List.of();

    public static int maxOvercharge;
    public static String overchargeColor = "";
    public static boolean overchargePreventsDurability;
    public static boolean overchargePreventsExperience;
    public static Map<String, Double> overchargeIngredients = Map.of();

    public static int staffsDefaultCost;
    public static int staffsDefaultCharge;

    public static List<Map<String, Map<String, Object>>> staffEntries = List.of();
}
