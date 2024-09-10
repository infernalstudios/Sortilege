package net.lyof.sortilege.config;

import com.google.gson.Gson;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.loader.api.FabricLoader;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.util.MathHelper;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

public class ModConfig {
    public static final ConfigEntry<Double> VERSION = new ConfigEntry<>("TECHNICAL.VERSION_DO_NOT_EDIT", 0d);
    public static final ConfigEntry<Boolean> RELOAD = new ConfigEntry<>("TECHNICAL.FORCE_RESET", false);

    public static Map CONFIG = new TreeMap<>();

    public static List<Pair<String, StaffInfo>> STAFFS = new ArrayList<>();



    public static class StaffInfo {
        public ToolMaterial tier;
        public int enchantability;
        public int damage;
        public int pierce;
        public int range;
        public int durability;
        public Supplier<Ingredient> repair;
        public int cooldown;
        public int charge_time;
        public int xp_cost;
        public List<Triple<Float, Float, Float>> colors = new ArrayList<>();
        public boolean fireRes;
        public String dependency;

        public String on_shoot;
        public String on_hit_self;
        public String on_hit_target;

        public StaffInfo(Map<String, Object> dict) {
            this(
                    (String) dict.getOrDefault("tier", "WOOD"),
                    MathHelper.toInt(dict.getOrDefault("enchantability", -1)),
                    MathHelper.toInt(dict.getOrDefault("damage", 2)),
                    MathHelper.toInt(dict.getOrDefault("pierce", 1)),
                    MathHelper.toInt(dict.getOrDefault("range", 8)),
                    MathHelper.toInt(dict.getOrDefault("durability", -1)),
                    (String) dict.getOrDefault("repair_item", ""),
                    MathHelper.toInt(dict.getOrDefault("cooldown", 15)),
                    MathHelper.toInt(dict.getOrDefault("charge_time", ConfigEntries.staffsDefaultCharge)),
                    MathHelper.toInt(dict.getOrDefault("xp_cost", ConfigEntries.staffsDefaultCost)),
                    (List<List<Double>>) dict.getOrDefault("beam_color", new ArrayList<>()),
                    dict.containsKey("fire_resistant") && (boolean) dict.get("fire_resistant"),
                    (String) dict.getOrDefault("dependency", "minecraft"),
                    (String) dict.getOrDefault("on_shoot", ""),
                    (String) dict.getOrDefault("on_hit_self", ""),
                    (String) dict.getOrDefault("on_hit_target", "")
            );
        }

        public StaffInfo(String tier, int enchant, int dmg, int pierce, int range, int dura, String repair,  int cooldown, int charge_time,
                         int xp_cost, List<List<Double>> colors, boolean fire_res, String dependency, String on_shoot, String on_hit_self, String on_hit_target) {
            try {
                this.tier = ToolMaterials.valueOf(tier);
            }
            catch (IllegalArgumentException e) {
                this.tier = ToolMaterials.WOOD;
            }
            this.enchantability = enchant == -1 ?
                this.tier.getEnchantability() : enchant;
            this.damage = dmg;
            this.pierce = pierce;
            this.range = range;
            this.durability = (dura == -1) ?
                this.tier.getDurability() : dura;
            this.repair = (repair.equals("")) ?
                    () -> this.tier.getRepairIngredient() : () -> Ingredient.ofItems(Registries.ITEM.get(new Identifier(repair)));
            this.cooldown = Math.max(cooldown, 0);
            this.charge_time = Math.max(charge_time, 1);
            this.xp_cost = xp_cost;
            try {
                for (List<Double> triple : colors)
                    this.colors.add(new MutableTriple<>(triple.get(0).floatValue(), triple.get(1).floatValue(), triple.get(2).floatValue()));
            }
            catch (Exception e) {
                Sortilege.log("Encountered an error while parsing a Staff's beam color");
            }
            this.fireRes = fire_res;
            this.dependency = dependency;

            this.on_shoot = on_shoot;
            this.on_hit_self = on_hit_self;
            this.on_hit_target = on_hit_target;
        }

        @Override
        public String toString() {
            return "StaffInfo{" +
                    "tier=" + tier +
                    ", enchantability=" + enchantability +
                    ", damage=" + damage +
                    ", pierce=" + pierce +
                    ", range=" + range +
                    ", durability=" + durability +
                    ", repair=" + Arrays.toString(repair.get().getMatchingStacks()) +
                    ", cooldown=" + cooldown +
                    ", charge_time=" + charge_time +
                    ", xp_cost=" + xp_cost +
                    ", color=" + colors +
                    ", fire_res=" + fireRes +
                    ", dependency='" + dependency + '\'' +
                    ", on_shoot='" + on_shoot + '\'' +
                    ", on_hit_self='" + on_hit_self + '\'' +
                    ", on_hit_target='" + on_hit_target + '\'' +
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
        String path = FabricLoader.getInstance().getConfigDir().resolve(Sortilege.MOD_ID + ".json").toString();

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

        if (!force && RELOAD.get()) {
            register(true);
            return;
        }


        List<Pair<String, StaffInfo>> result = new ArrayList<>();
        for (Map<String, Map<String, Object>> staff : ConfigEntries.staffEntries) {
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
                "VERSION_DO_NOT_EDIT": 1.5,
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
                  // Should curses add enchantment slots instead of using them
                  "curses_add_slots": true,
                  // Override modes include "relative" and "absolute".
                  //    If set to "relative", the overrides defined below will be added onto the default limit.
                  //    If set to "absolute", they'll replace the default limits.
                  "override_mode": "relative",
                  // Overrides to the amount of enchantments an item can have. Must be of the form "modid:itemid": value
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
                    "sortilege:golden_staff": 2,
                    
                    "miningmaster:ultima_sword": 1,
                    "miningmaster:ultima_pickaxe": 1
                  },
                  // Should an item's maximum enchantments be displayed even when it is not enchanted
                  "always_show_limit": true,
                  "limitite": {
                    // Should Limitite have an enchantment glint
                    "is_foil": true,
                    // 1 in X chance for Limitite to spawn as loot in chests. Set to 0 or lower to disable it
                    "loot_weight": 24,
                    // How many Limitites can be applied to a single item
                    "max_limit_break": 3
                  }
                },
                // Allow using Enchanted Books on items in inventory
                "allow_inventory_enchanting": false,
                
                // Should the Magic Protection enchantment be compatible with vanilla Protection enchantments
                "magic_protection_protection_compatibility": false,
                // Feather Falling at this level completely negates fall damage (should be set to the maximum if enabled, or higher if disabled)
                "better_feather_falling": 4,
                // Unbreaking at this level makes an item completely unbreakable (should be set to the maximum if enabled, or higher if disabled)
                "better_unbreaking": 3,
                // Should Magic Protection also give a level*5 chance to dodge any attack
                "better_magic_protection": true,
                // Should Fire Protection at this level on every armor slot at once grant fire immunity
                "better_fire_protection": 4
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
                "costs": [1, 3, 7],
                // If increased_enchant_costs is true, defines the required xp levels to enchants
                "needed": [5, 10, 15],
                // Should Anvils never cost experience
                "no_xp_anvil": true,
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
                },
                
                // Locks certain recipes behind experience levels or advancements.
                //    Each entry must be of the form "modid:recipeid": minimalxplevel or "modid:recipeid": "advancementid"
                //    The default config locks the crafting of Ender Eyes behind level 30 and the Beacon behind summoning the Wither, as an example
                "recipe_locks": {
                  "minecraft:ender_eye": 30,
                  "minecraft:beacon": "minecraft:nether/summon_wither"
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
                // Keep equipped items (armor and hotbar) on death
                "keep_equipped": false,
                // Should the Soulbound enchantment be removed on use
                "consume_soulbound": false,
                // Display death coordinates instead of the score from vanilla on the death screen
                "show_coordinates_on_death": true
              },
              
              // CATEGORY: BREWING
              "brewing": {
                // A list of potion effects for which Antidotes don't get registered
                "antidote_blacklist": [
                ],
                "antidote_stack_size": 4
              },
              
              // CATEGORY: STAFFS
              "staffs": {
                // Amount of xp points needed to use a staff, if not set
                "default_xp_cost": 0,
                // Time staffs need to be held down (in ticks) before shooting, if not set
                "default_charge_time": 1,
                "entries": [
                  {
                    // Example entry, not loaded in game as it's only for demonstration purposes
                    "example_staff": {
                      // Sets the repair material and the durability if not set
                      "tier": "GOLD",
                      // Staff's enchantability on the Enchanting Table. Defaults to the tier's
                      "enchantability": 22,
                      // Half hearts of damage the staff deals
                      "damage": 5,
                      // Maximal number of targets the staff can pierce through
                      "pierce": 2,
                      // Range of the staff, in half blocks
                      "range": 10,
                      // Durability of the staff. Defaults to tier's
                      "durability": 512,
                      // Item to be used to repair the staff. Defaults to the tier's
                      "repair_item": "minecraft:obsidian",
                      // Amount of ticks to wait for between each shots
                      "cooldown": 20,
                      // Amount of ticks of casting to shoot. Defaults to default_charge_time above
                      "charge_time": 1,
                      // Amount of xp points needed to shoot. Defaults to default_xp_cost above
                      "xp_cost": 0,
                      // Custom RGB colors to be used for the staff's beam. If unset, the beam will be white unless the staff has enchantments
                      "beam_color": [
                        [0.5, 0, 0],
                        [0, 0.5, 0],
                        [0, 0, 0.5]
                      ],
                      // Whether the staff is resistant to fire like Netherite items. Defaults to false
                      "fire_res": true,
                      // Mod needed to be loaded for the staff to appear in game. Defaults to minecraft
                      "dependency": "nah",
                      // Commands to be run when using the staff
                      "on_shoot": "/give @s minecraft:lapis_lazuli",
                      "on_hit_self": "/effect give @s minecraft:regeneration",
                      "on_hit_target": "/tp @s ~ ~2 ~"
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
