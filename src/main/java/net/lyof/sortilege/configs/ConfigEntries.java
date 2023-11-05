package net.lyof.sortilege.configs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigEntries {
    public static void reload() {
        WitchHatDropChance = new ConfigEntry<>("witch_hat.drop_chance", 0.1).get();
        WitchHatBonus = new ConfigEntry<>("witch_hat.xp_bonus", 3).get();

        EnchantLimiterDefault = new ConfigEntry<>("enchantments.enchant_limiter.default", 3).get();
        EnchantLimiterMode = new ConfigEntry<>( "enchantments.enchant_limiter.override_mode", "relative").get();

        MagicProtCompatibility = new ConfigEntry<>("enchantments.magic_protection_protection_compatibility", false).get();

        DoIncreasedEnchantCosts = new ConfigEntry<>("experience.increased_enchant_costs", true).get();
        IncreasedEnchantCosts = new ConfigEntry<>("experience.costs", List.of(5d, 15d, 30d)).get();
        xpLevelCap = new ConfigEntry<>("experience.level_cap", 100).get();

        ShowDeathCoordinates = new ConfigEntry<>("show_coordinates_on_death", true).get();

        StaffsHdParticles = new ConfigEntry<>("staffs.use_hd_particles", false).get();
        StaffsDefaultCost = new ConfigEntry<>("staffs.default_xp_cost", 0).get();
        StaffsDefaultCharge = new ConfigEntry<>("staffs.default_charge_time", 1).get();

        StaffEntries = new ConfigEntry<List<Map<String, Map<String, ?>>>>("staffs.entries", new ArrayList()).get();
    }

    public static double WitchHatDropChance;
    public static int WitchHatBonus;

    public static int EnchantLimiterDefault;
    public static String EnchantLimiterMode;

    public static boolean MagicProtCompatibility;

    public static boolean DoIncreasedEnchantCosts;
    public static List<Double> IncreasedEnchantCosts;
    public static int xpLevelCap;

    public static boolean ShowDeathCoordinates;

    public static boolean StaffsHdParticles;
    public static int StaffsDefaultCost;
    public static int StaffsDefaultCharge;

    public static List<Map<String, Map<String, ?>>> StaffEntries;
}
