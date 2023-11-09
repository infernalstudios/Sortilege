package net.lyof.sortilege.configs;

import com.google.gson.Gson;
import com.mojang.datafixers.util.Pair;
import net.lyof.sortilege.Sortilege;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ModJsonConfigs {
    public static final ConfigEntry<Double> VERSION = new ConfigEntry<>("TECHNICAL.VERSION_DO_NOT_EDIT", 0d);
    public static final ConfigEntry<Boolean> RELOAD = new ConfigEntry<>("TECHNICAL.FORCE_RESET", false);

    public static Map CONFIG = new TreeMap<>();

    public static List<Pair<String, StaffInfo>> STAFFS = new ArrayList<>();



    public static class StaffInfo {
        public Tier tier;
        public int damage;
        public int pierce;
        public int range;
        public int durability;
        public int cooldown;
        public int charge_time;
        public int xp_cost;
        public boolean fireRes;
        public String dependency;

        public StaffInfo(Map<String, ?> dict) {
            this(
                    dict.containsKey("tier") ? (String) dict.get("tier") : "WOOD",
                    dict.containsKey("damage") ?  (int) Math.round((double) dict.get("damage")) : 2,
                    dict.containsKey("pierce") ? (int) Math.round((double) dict.get("pierce")) : 1,
                    dict.containsKey("range") ? (int) Math.round((double) dict.get("range")) : 8,
                    dict.containsKey("durability") ? (int) Math.round((double) dict.get("durability")) : -1,
                    dict.containsKey("cooldown") ? (int) Math.round((double) dict.get("cooldown")) : 15,
                    dict.containsKey("charge_time") ? (int) Math.round((double) dict.get("charge_time")) : ConfigEntries.StaffsDefaultCharge,
                    dict.containsKey("xp_cost") ? (int) Math.round((double) dict.get("xp_cost")) : ConfigEntries.StaffsDefaultCost,
                    dict.containsKey("fire_resistant") && (boolean) dict.get("fire_resistant"),
                    dict.containsKey("dependency") ? String.valueOf(dict.get("dependency")) : "minecraft"
            );
        }

        public StaffInfo(String tier, int dmg, int pierce, int range, int dura, int cooldown, int charge_time,
                         int xp_cost, boolean fire_res, String dependency) {
            try {
                this.tier = Tiers.valueOf(tier);
            }
            catch (IllegalArgumentException e) {
                this.tier = Tiers.WOOD;
            }
            this.damage = dmg;
            this.pierce = pierce;
            this.range = range;
            this.durability = (dura == -1) ?
                (int) Math.round(this.tier.getUses() * 0.7) : dura;
            this.cooldown = Math.max(cooldown, 0);
            this.charge_time = Math.max(charge_time, 1);
            this.xp_cost = xp_cost;
            this.fireRes = fire_res;
            this.dependency = dependency;
        }

        @Override
        public String toString() {
            return "StaffInfo{" +
                    "tier=" + tier +
                    ", damage=" + damage +
                    ", pierce=" + pierce +
                    ", range=" + range +
                    ", durability=" + durability +
                    ", cooldown=" + cooldown +
                    ", charge_time=" + charge_time +
                    ", xp_cost=" + xp_cost +
                    ", fireRes=" + fireRes +
                    ", dependency=" + dependency +
                    '}';
        }
    }


    public static <T> T get(String path, T fallback) {
        return new ConfigEntry<>(path, fallback).get();
    }

    public static void register() {
        register(false);
    }

    public static void register(boolean force) {
        String path = System.getProperty("user.dir") + File.separator +
                "config" + File.separator + Sortilege.MOD_ID + "-common.json";

        Sortilege.log("Loading Configs for Sortilege");

        // Create config file if it doesn't exist already
        File config = new File(path);
        boolean create = !config.isFile();

        if (create || force) {
            try {
                config.delete();
                config.createNewFile();

                FileWriter writer = new FileWriter(path);
                writer.write(DEFAULT_CONFIG);
                writer.close();

                Sortilege.log("Sortilege Config file created");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }


        String configContent = DEFAULT_CONFIG;
        try {
            configContent = FileUtils.readFileToString(config, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        CONFIG = new Gson().fromJson(parseJson(configContent), Map.class);
        ConfigEntries.reload();

        if (!force && (RELOAD.get() || VERSION.get() < getVersion())) {
            register(true);
            return;
        }


        List<Pair<String, StaffInfo>> result = new ArrayList<>();
        for (Map<String, Map<String, ?>> staff : ConfigEntries.StaffEntries) {
            String id = String.valueOf(List.of(staff.keySet().toArray()).get(0));
            result.add(new Pair<>(id, new StaffInfo(staff.get(id))));
        }
        STAFFS = result;
    }

    public static String parseJson(String text) {
        StringBuilder result = new StringBuilder();

        for (String line : text.split("\n")) {
            if (!line.strip().startsWith("//"))
                result.append("\n").append(line);
        }

        Sortilege.log(result);
        return result.toString();
    }

    public static double getVersion() {
        String text = DEFAULT_CONFIG;
        int start = 0;

        while (!List.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.').contains(text.charAt(start))) {
            start++;
        }
        int end = start + 1;
        while (List.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.').contains(text.charAt(end))) {
            end++;
        }

        return Double.parseDouble(text.substring(start, end));
    }


    public static final String DEFAULT_CONFIG = """
            {
              "TECHNICAL": {
                "VERSION_DO_NOT_EDIT": 1.3,
                "FORCE_RESET": false
              },
              
              // This config file uses a custom defined parser. That's why there are comments here, they wouldn't be valid in any other .json file.
              //    To add a comment yourself, just start a line with // like I did here
              //    (although their main use is explaining you what the entries do)
              
              // CATEGORY: ENCHANTING
              "enchantments": {
                "enchant_limiter": {
                  // Limits how many enchantments can be added to an item. Set it to -1 to disable the limiter, 
                  //    and to 0 to disable enchanting as a whole
                  "default": 3,
                  // Either "relative" or "absolute". If relative, the overrides defined below will be summed with the default limit.
                  //    If absolute, they'll replace it
                  "override_mode": "relative",
                  // Overrides to the amount of enchantments an item can have. Must be of the form "itemid": value
                  "overrides": {
                    "minecraft:golden_shovel": 2,
                    "minecraft:golden_pickaxe": 2,
                    "minecraft:golden_axe": 2,
                    "minecraft:golden_hoe": 2,
                    "minecraft:golden_sword": 2,
                    "minecraft:golden_helmet": 2,
                    "minecraft:golden_chestplate": 2,
                    "minecraft:golden_leggings": 2,
                    "minecraft:golden_boots": 2,
                    "sortilege:golden_staff": 2
                  },
                  // Should an item's maximum enchantments be displayed even when it is unenchanted
                  "always_show_limit": true,
                  // Should Limitite spawn in rare structures' chests
                  "generate_limitite_loot": true
                },
                // Should the Magic Protection enchantment be compatible with vanilla Protection enchantments
                "magic_protection_protection_compatibility": false
              },
              
              // CATEGORY: EXPERIENCE
              "experience": {
                "witch_hat": {
                  // Chance for the Witch Hat to drop when killing a Witch. Set to 0 to disable the drop
                  "drop_chance": 0.1,
                  // How many extra experience points should drop when killing a monster with the Witch Hat equipped
                  "xp_bonus": 3
                },
                // Should enchanting in an enchanting table cost more xp than the default 1 2 3 levels
                "increased_enchant_costs": true,
                // If the above is true, defines the new costs to replace 1 2 3
                "costs": [5, 15, 30],
                // Maximum experience level a player can have before it can't increase anymore. Set to -1 to disable the limit,
                //    and to 0 to disable experience
                "level_cap": 100,
                // How much xp points are needed to level up, in place of the exponential formula vanilla has.
                //    Set to 0 or lower to use vanilla's formula
                "linear_xp_requirement": 50,
                // Should monsters have a chance to give a bunch of extra experience points when killed
                "xp_bounty": {
                  // Should the sortilege:bounties tag act as a whitelist instead of a blacklist. It defines which mobs can drop bounties
                  "tag_is_whitelist": false,
                  // Amount of xp points bounties drop
                  "value": 20,
                  // Chance for a bounty to happen
                  "chance": 0.05
                }
              },
              
              // CATEGORY: DEATH
              "death": {
                // Enable a balanced keepInventory only for experience
                "xp_keeping": {
                  "enable": true,
                  // Should players killed by players drop their xp or give it to their assassin directly
                  "allow_stealing_from_players": true,
                  // Ratio of xp kept on death
                  "self_ratio": 0.3,
                  // Ratio of xp stolen by the attacker, and dropped back when it's killed
                  "attacker_ratio": 0.6,
                  // Ratio of xp dropped on the ground on death
                  "drop_ratio": 0.1
                },
                // Display death coordinates instead of the score from vanilla on the death screen
                "show_coordinates_on_death": true
              },
              
              // CATEGORY: STAFFS
              "staffs": {
                // Use high resolution particles instead of the default pixelated ones
                "use_hd_particles": false,
                // Amount of xp points needed to use a staff, if not set
                "default_xp_cost": 0,
                // Time staffs need to be held down (in ticks) before shooting, if not set
                "default_charge_time": 1,
                "entries": [
                  {
                    // Example entry, not loaded in game as it's only for demonstration purposes
                    "example_staff": {
                      "tier": "GOLD",      // Sets the repair material and the durability if not set
                      "damage": 5,         // Half hearts of damage the staff deals
                      "pierce": 2,         // Maximal number of targets the staff can pierce through
                      "range": 10,         // Range of the staff, in half blocks
                      "durability": 512,   // Durability of the staff. Defaults to tier's * 0.7
                      "cooldown": 20,      // Amount of ticks to wait for between each shots
                      "charge_time": 1,    // Amount of ticks of casting to shoot. Defaults to default_charge_time above
                      "xp_cost": 0,        // Amount of xp points needed to shoot. Defaults to default_xp_cost above
                      "fire_res": true,    // Whether the staff is resistant to fire like Netherite items. Defaults to false
                      "dependency": "nah"  // Mod needed to be loaded for the staff to appear in game. Defaults to minecraft
                    }
                  },
                  // Actual staffs
                  {
                    "wooden_staff": {
                      "tier": "WOOD",
                      "damage": 3,
                      "pierce": 1,
                      "range": 6,
                      "cooldown": 15
                    }
                  },
                  {
                    "stone_staff": {
                      "tier": "STONE",
                      "damage": 4,
                      "pierce": 1,
                      "range": 8,
                      "cooldown": 20
                    }
                  },
                  {
                    "iron_staff": {
                      "tier": "IRON",
                      "damage": 5,
                      "pierce": 1,
                      "range": 10,
                      "cooldown": 15
                    }
                  },
                  {
                    "golden_staff": {
                      "tier": "GOLD",
                      "damage": 3,
                      "pierce": 2,
                      "range": 14,
                      "cooldown": 10
                    }
                  },
                  {
                    "diamond_staff": {
                      "tier": "DIAMOND",
                      "damage": 5,
                      "pierce": 2,
                      "range": 12,
                      "cooldown": 15
                    }
                  },
                  {
                    "netherite_staff": {
                      "tier": "NETHERITE",
                      "damage": 6,
                      "pierce": 3,
                      "range": 16,
                      "fire_resistant": true,
                      "cooldown": 20
                    }
                  }
                ]
              }
            }""";
}
