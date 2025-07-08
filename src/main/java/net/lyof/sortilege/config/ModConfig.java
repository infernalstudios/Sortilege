package net.lyof.sortilege.config;

import com.google.gson.Gson;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.loader.api.FabricLoader;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.util.MathHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

public class ModConfig {
    public static final ConfigEntry<Double> VERSION = new ConfigEntry<>("TECHNICAL.VERSION_DO_NOT_EDIT", 0d);
    public static final ConfigEntry<Boolean> RELOAD = new ConfigEntry<>("TECHNICAL.FORCE_RESET", false);

    public static Map CONFIG = new HashMap<>();

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
        public List<float[]> colors = new ArrayList<>();
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
            if (repair.isEmpty())
                this.repair = () -> this.tier.getRepairIngredient();
            if (repair.startsWith("#")) {
                TagKey<Item> tag = TagKey.of(RegistryKeys.ITEM, new Identifier(repair.substring(1)));
                this.repair = () -> Ingredient.fromTag(tag);
            }
            else {
                Identifier id = new Identifier(repair);
                this.repair = () -> Ingredient.ofItems(Registries.ITEM.get(id));
            }
            this.cooldown = Math.max(cooldown, 0);
            this.charge_time = Math.max(charge_time, 1);
            this.xp_cost = xp_cost;
            try {
                for (List<Double> triple : colors)
                    this.colors.add(new float[]{triple.get(0).floatValue(), triple.get(1).floatValue(), triple.get(2).floatValue()});
            }
            catch (Exception e) {
                Sortilege.log("Encountered an error while parsing a Staff's beam color", 2);
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

                Sortilege.log("Sortilege Config file created", 3);
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

        if (getVersion() > VERSION.get())
            Sortilege.log("Your Sortilege configs are outdated! Consider deleting them so they can refresh", 1);


        List<Pair<String, StaffInfo>> result = new ArrayList<>();
        for (Map<String, Map<String, Object>> staff : ConfigEntries.staffEntries) {
            String id = String.valueOf(staff.keySet().toArray()[0]);
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
                "VERSION_DO_NOT_EDIT": 2.0,
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
                
                "enchant_catalyst": {
                  // Should Enchanted Books be usable as catalysts to increase the odds of getting their enchantments
                  // If this is set to false and no catalyst is loaded from datapacks, the module will be disabled
                  "allow_books": true,
                  // Chance (0 - 1) for non book catalysts to activate for each option in the enchanting table
                  "activation_chance": 0.5,
                  // Should items usable as catalysts display it in their tooltip
                  "show_in_tooltip": true,
                  
                  // If true, the default enchant logic will be removed, meaning a catalyst *must* be used in order to get enchants at an enchanting table
                  "override_default_enchanting": false
                },
                
                // Allow using Enchanted Books on items in inventory
                "allow_inventory_enchanting": false,
                // Should Mining Master's gem smithing recipes be edited so that they also give staff enchantments
                "mining_master_integration": true,
                
                // Should the Magic Protection enchantment be compatible with vanilla Protection enchantments
                "magic_protection_protection_compatibility": false,
                // Feather Falling at this level completely negates fall damage (should be set to the maximum if enabled, or higher if disabled)
                "better_feather_falling": 4,
                // Unbreaking at this level makes an item completely unbreakable (should be set to the maximum if enabled, or higher if disabled)
                "better_unbreaking": 3,
                // Should Magic Protection also give a level*5 chance to dodge any attack
                "better_magic_protection": true,
                // Should Fire Protection at this level on every armor slot at once grant fire immunity
                "better_fire_protection": 4,
                // Should Bane of Arthropods apply a 0.5*(level + 1) seconds of slowness on hit
                "better_bane_of_arthropods": true,
                
                // Set these to false to disable the corresponding enchantment from appearing in game (it won't be registered)
                "enabled_enchants": {
                  "potency": true,
                  "stability": true,
                  "chaining": true,
                  "focus": true,
                  "wisdom": true,
                  "push": true,
                  "pull": true,
                  "brazier": true,
                  "blizzard": true,
                  "blast": true,
                  "blitz": true,
                  "bonk": true,
                  "ignorance_curse": true,
                  
                  "magic_protection": true,
                  "arcane": true,
                  "soulbound": true
                }
              },
              
              // CATEGORY: EXPERIENCE
              "experience": {
                "witch_hat": {
                  // Should the Witch Hat be registered as an item
                  "enable": true,
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
                  "drop_ratio": 0.1,
                  // Entity data id to use to store stolen xp. Only change this if the default is causing conflicts
                  "stolen_xp_data": 41
                },
                // Keep equipped items (armor and hotbar) on death
                "keep_equipped": false,
                // Should the Soulbound enchantment be removed on use
                "consume_soulbound": false,
                // Display death coordinates instead of the score from vanilla on the death screen
                "show_coordinates_on_death": true,
                // Should the mob that killed you be made glowing
                "glowing_killer": true
              },
              
              // CATEGORY: BREWING
              "brewing": {
                "antidote": {
                  // Should Antidotes be registered as items
                  "enable": true,
                  // A list of potion effects for which Antidotes don't get registered
                  "effect_blacklist": [
                  ],
                  "stack_size": 16,
                  // For how many seconds Antidotes make you immune to their effect after drinking. Set to 0 or lower to disable extra immunity
                  "immunity_time": 300
                },
                "potion": {
                  "stack_size": 8,
                  // Value to multiply all potions effects length by.
                  //    For example, if this is 2, then all effects gained *from potions* will last twice as long
                  //    This has no effect on potions whose effects were overridden by datapack
                  "duration_multiplier": 1.5,
                  // How many ticks should drinking a potion take. 20t = 1s
                  "drinking_time": 20,
                  // How many ticks of cooldown potions get after being drank or thrown
                  "cooldown": 200
                },
                "cauldron": {
                  // Should cauldron brewing be enabled
                  "enable": true,
                  // To disable cauldrons from filling over time automatically if above a soul campfire,
                  //    override the #sortilege:refills_cauldrons block tag to be empty
                  // If true, dropping blaze powder into a cauldron containing potion will increment its fluid level by 1 (up to 3)
                  "blaze_powder_refill": true,
                  // If true, cauldrons generated in Swamp Huts will have a random potion inside
                  "fill_swamp_huts_randomly": true,
                  // A list of effect ids that are not allowed to generate in swamp huts
                  "swamp_hut_blacklist": [
                  ]
                }
              },
              
              // CATEGORY: EQUIPMENT
              "equipment": {
                "lapis_shield": {
                  // Should the Witch Hat be registered as an item
                  "enable": true,
                  // How many durability points do Lapis Shields have
                  "durability": 152,
                  // How many ticks between each Lapis Shield dodge, in ticks (20t = 1s)
                  "cooldown": 80
                },
              
                // CATEGORY: STAFFS
                "staffs": {
                  "overcharge": {
                    // Maximal overcharge a staff can hold at any given time
                    "max_overcharge": 20,
                    // Color for the overcharge bar. Hexadecimal format
                    "bar_color": "#0000ff",
                    // Should overcharged staffs not use durability
                    "free_durability": true,
                    // Should overcharged staffs not use experience
                    "free_experience": true,
                    // Which items can be used to overcharge staffs. Must be formatted as "modid:itemid": value
                    "ingredients": {
                      "minecraft:lapis_lazuli": 2,
                      "minecraft:lapis_block": 20
                    }
                  },
                
                  // Amount of xp points needed to use a staff, if not set
                  "default_xp_cost": 3,
                  // Time staffs need to be held down (in ticks) before shooting, if not set
                  "default_charge_time": 1,
                  // Should staffs be able to pierce through walls, at a rate of 1 block per pierce value
                  "pierce_blocks": false,
                  
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
                        "pierce": 2,
                        "range": 6,
                        "cooldown": 20
                      }
                    },
                    {
                      "stone_staff": {
                        "tier": "STONE",
                        "damage": 4,
                        "pierce": 1,
                        "range": 8,
                        "cooldown": 25
                      }
                    },
                    {
                      "iron_staff": {
                        "tier": "IRON",
                        "damage": 5,
                        "pierce": 1,
                        "range": 10,
                        "cooldown": 20
                      }
                    },
                    {
                      "crystalline_staff": {
                        "enchantability": 19,
                        "damage": 4,
                        "pierce": 3,
                        "range": 10,
                        "cooldown": 20,
                        "durability": 612,
                        "repair_item": "phantasm:crystal_shard",
                        "dependency": "phantasm"
                        // XP speed boost handled by the native Phantasm tag
                      }
                    },
                    {
                      "golden_staff": {
                        "tier": "GOLD",
                        "damage": 2,
                        "pierce": 2,
                        "range": 14,
                        "cooldown": 15
                      }
                    },
                    {
                      "silver_staff": {
                        "durability": 157,
                        "repair_item": "#forge:ingots/silver",
                        "enchantability": 18,
                        "damage": 4,
                        "pierce": 2,
                        "range": 15,
                        "cooldown": 20,
                        "dependency": "caverns_and_chasms"
                      }
                    },
                    {
                      "diamond_staff": {
                        "tier": "DIAMOND",
                        "damage": 5,
                        "pierce": 2,
                        "range": 12,
                        "cooldown": 20
                      }
                    },
                    {
                      "gripcrystal_staff": {
                        "damage": 5,
                        "pierce": 2,
                        "range": 12,
                        "cooldown": 15,
                        "repair_item": "unseen_world:gripcrystal",
                        "dependency": "unseen_world",
                        "beam_color": [
                          [0.25, 0, 1],
                          [0.35, 0.25, 1]
                        ],
                        "on_hit_self": "/set_gripcrystal_mana 2"
                      }
                    },
                    {
                      "spawner_staff": {
                        "damage": 4,
                        "pierce": 3,
                        "range": 14,
                        "cooldown": 25,
                        "durability": 1562,
                        "repair_item": "dungeonnowloading:spawner_blade",
                        "dependency": "dungeonnowloading",
                        "on_hit_self": "/effect give @s minecraft:regeneration 4 0 true",
                        "beam_color": [
                          [0.5, 0.2, 0.5],
                          [0.4, 0.1, 0.4],
                          [0.25, 0, 0.25]
                        ]
                      }
                    },
                    {
                      "divine_beryl_staff": {
                        "durability": 1851,
                        "repair_item": "miningmaster:divine_beryl",
                        "enchantability": 15,
                        "damage": 5,
                        "pierce": 3,
                        "range": 14,
                        "cooldown": 25,
                        "dependency": "miningmaster",
                        "beam_color": [
                          [0, 0.4, 0.1]
                        ]
                        // will have blitz 4 applied
                      }
                    },
                    {
                      "heart_rhodonite_staff": {
                        "durability": 1851,
                        "repair_item": "miningmaster:heart_rhodonite",
                        "enchantability": 15,
                        "damage": 5,
                        "pierce": 3,
                        "range": 14,
                        "fire_resistant": false,
                        "cooldown": 25,
                        "dependency": "miningmaster",
                        "on_hit_self": "/effect give @s minecraft:instant_health",
                        "beam_color": [
                          [1, 0.4, 0.5]
                        ]
                      }
                    },
                    {
                      "spider_kunzite_staff": {
                        "durability": 1851,
                        "repair_item": "miningmaster:spider_kunzite",
                        "enchantability": 15,
                        "damage": 5,
                        "pierce": 3,
                        "range": 14,
                        "fire_resistant": false,
                        "cooldown": 25,
                        "dependency": "miningmaster",
                        "on_hit_target": "/effect give @s minecraft:poison 3 1",
                        "beam_color": [
                          [1, 0.8, 0.8]
                        ]
                      }
                    },
                    {
                      "electrum_staff": {
                        "durability": 1561,
                        "repair_item": "oreganized:electrum_ingot",
                        "enchantability": 14,
                        "damage": 4,
                        "pierce": 3,
                        "range": 16,
                        "cooldown": 20,
                        "dependency": "oreganized",
                        "on_shoot": "/effect give @s minecraft:speed 5 0",
                        "on_hit_self": "/effect give @s minecraft:haste 3 0"
                      }
                    },
                    {
                      "netherite_staff": {
                        "tier": "NETHERITE",
                        "damage": 6,
                        "pierce": 3,
                        "range": 16,
                        "fire_resistant": true,
                        "cooldown": 25
                      }
                    },
                    {
                      "pearlescent_staff": {
                        "damage": 7,
                        "pierce": 1,
                        "range": 20,
                        "cooldown": 25,
                        "repair_item": "unusualend:pearlescent_ingot",
                        "dependency": "unusualend",
                        "on_shoot": "/effect give @s minecraft:speed 3 0 true",
                        "beam_color": [
                          [0.5, 0, 0.75],
                          [0.75, 0.25, 1]
                        ]
                      }
                    },
                    {
                      "necromium_staff": {
                        "durability": 2031,
                        "repair_item": "caverns_and_chasms:necromium_ingot",
                        "enchantability": 15,
                        "damage": 5,
                        "pierce": 2,
                        "range": 16,
                        "fire_resistant": true,
                        "cooldown": 25,
                        "on_hit_target": "/effect give @e[distance=..2] slowness 5 2",
                        "dependency": "caverns_and_chasms"
                      }
                    },
                    {
                      "enderite_staff": {
                        "durability": 4096,
                        "repair_item": "enderitemod:enderite_ingot",
                        "enchantability": 17,
                        "damage": 7,
                        "pierce": 3,
                        "range": 16,
                        "fire_resistant": true,
                        "cooldown": 25,
                        "on_hit_target": "/tp @s ~ ~5 ~",
                        "dependency": "enderitemod"
                      }
                    },
                    {
                      "ultima_staff": {
                        "durability": 2341,
                        "repair_item": "minecraft:diamond",
                        "enchantability": 20,
                        "damage": 7,
                        "pierce": 3,
                        "range": 16,
                        "cooldown": 20,
                        "dependency": "miningmaster",
                        "on_hit_target": "/effect give @s minecraft:poison 3 1",
                        "on_hit_self": "/effect give @s minecraft:instant_health",
                        "beam_color": [
                          [0, 0.4, 0.1],
                          [1, 0.4, 0.5],
                          [1, 0.8, 0.8]
                        ]
                        // will have blitz 4 applied
                      }
                    },
                    {
                      "neptunium_staff": {
                        "durability": 1796,
                        "repair_item": "aquaculture:neptunium_ingot",
                        "enchantability": 14,
                        "damage": 6,
                        "pierce": 1,
                        "range": 12,
                        "cooldown": 20,
                        "beam_color": [
                          [0.25, 0.46, 0.89],
                          [0.27, 0.67, 0.95],
                          [0.24, 0.34, 0.84]
                        ],
                        "on_hit_target": "/setblock ~ ~ ~ minecraft:water[level=15] keep",
                        "dependency": "aquaculture"
                      }
                    }
                  ]
                }
              }
            }""";
}
