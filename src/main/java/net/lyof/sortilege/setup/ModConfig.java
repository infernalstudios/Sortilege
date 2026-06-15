package net.lyof.sortilege.setup;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.lcc.sollib.api.common.SolRegistries;
import net.lcc.sollib.api.common.config.ConfigEntry;
import net.lcc.sollib.api.common.config.builder.IJsonBuilder;
import net.lcc.sollib.core.Identifier;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.staff.OverchargeEntry;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.recipe.crafting.RecipeLock;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class ModConfig {
    public static void build(IJsonBuilder builder) {
        builder.addObject("enchanting", enchanting -> enchanting
                .addObject("limiter", limiter -> limiter
                        .comment("Limits how many enchantments can be added to an item. Set it to -1 to disable the limiter,")
                        .comment("  and to 0 to disable enchanting as a whole")
                        .add("default", 3)
                        .bind(enchantLimiterDefault)
                        .comment("Should curses add enchantment slots instead of using them")
                        .add("curses_add_slots", true)
                        .bind(cursesAddSlots)
                        .comment("Override modes include \"relative\" and \"absolute\".")
                        .comment("  If set to \"relative\", the overrides defined below will be added onto the default limit.")
                        .comment("  If set to \"absolute\", they will replace the default limits.")
                        .add("override_mode", "relative")
                        .bind(enchantLimiterMode)
                        .comment("Overrides to the amount of enchantments an item can have. Must be of the form \"modid:itemid\": value")
                        .addObject("overrides", overrides -> overrides
                                .add("minecraft:golden_shovel", 2)
                                .add("minecraft:golden_pickaxe", 2)
                                .add("minecraft:golden_axe", 2)
                                .add("minecraft:golden_hoe", 2)
                                .add("minecraft:golden_sword", 2)
                                .add("minecraft:golden_helmet", 2)
                                .add("minecraft:golden_chestplate", 2)
                                .add("minecraft:golden_leggings", 2)
                                .add("minecraft:golden_boots", 2)
                                .add("sortilege:golden_staff", 2)
                                .comment("")
                                .add("miningmaster:ultima_sword", 1)
                                .add("miningmaster:ultima_pickaxe", 1)
                        )
                        .bind(enchantLimiterOverrides)
                        .comment("Should an item's maximum enchantments be displayed even when it is not enchanted")
                        .add("always_show_limit", true)
                        .bind(alwaysShowEnchantLimit)
                        .addObject("limitite", limitite -> limitite
                                .comment("Should Limitite have an enchantment glint")
                                .add("has_glint", true)
                                .bind(limititeHasGlint)
                                .comment("1 in X chance for Limitite to spawn as loot in chests. Set to 0 or lower to disable it")
                                .add("loot_weight", 24)
                                .bind(limititeLootWeight)
                                .comment("How many Limitites can be applied to a single item")
                                .add("max_limit_break", 3)
                                .bind(maxLimitBreak)
                        )
                )
                .comment("")
                .addObject("enchanting_table", enchanting_table -> enchanting_table
                        .addObject("catalyst", catalyst -> catalyst
                                .comment("Should Enchanted Books be usable as catalysts to increase the odds of getting their enchantments")
                                .comment("  If this is set to false and no catalyst is loaded from datapacks, the module will be disabled")
                                .add("allow_books", true)
                                .bind(catalystBooks)
                                .comment("Chance (0 - 1) for non book catalysts to activate for each option in the enchanting table")
                                .add("activation_chance", 0.5)
                                .bind(catalystChance)
                                .comment("Should items usable as catalysts display it in their tooltip")
                                .add("show_in_tooltip", true)
                                .bind(catalystTooltip)
                                .comment("")
                                .comment("If true, enchanting tables will only be able to apply catalysts and won't provide enchants by themselves")
                                .add("override_default", false)
                                .bind(catalystOnly)
                        )
                        .comment("")
                        .addObject("knowledge", knowledge -> knowledge
                                .comment("Toggle this module. If true, enchants must be discovered as loot or catalyzed before they can be obtained at an enchanting table")
                                .add("enable", true)
                                .bind(knowledgeEnabled)
                                .comment("Should loot items that can be learnt from display it in their tooltip")
                                .add("show_in_tooltip", true)
                                .bind((knowledgeTooltip))
                        )
                )
                .comment("")
                .comment("Allow using Enchanted Books on items in inventory")
                .add("inventory_enchanting", false)
                .bind(allowInventoryEnchanting)
                .comment("Should Mining Master's gem smithing recipes be edited so that they also give staff enchantments")
                .add("mining_master_integration", true)
                .bind(miningMasterIntegration)
                .comment("")
                .comment("Should the Magic Protection enchantment be compatible with vanilla Protection enchantments")
                .add("magic_protection_compatibility", false)
                .bind(magicProtCompatibility)
                .comment("Feather Falling at this level completely negates fall damage (should be set to the maximum if enabled, or -1 if disabled)")
                .add("extended_feather_falling", 4)
                .bind(expandedFeatherFalling)
                .comment("Unbreaking at this level makes an item completely unbreakable (should be set to the maximum if enabled, or -1 if disabled)")
                .add("extended_unbreaking", 3)
                .bind(expandedUnbreaking)
                .comment("Should Magic Protection also give a level*5% chance to dodge any attack")
                .add("extended_magic_protection", true)
                .bind(expandedMagicProt)
                .comment("Having a full set Fire Protection at this level will grant fire immunity  (should be set to the maximum if enabled, or -1 if disabled)")
                .add("extended_fire_protection", 4)
                .bind(expandedFireProt)
                .comment("Should Bane of Arthropods apply a 0.5*(level + 1) seconds of slowness on hit")
                .add("extended_bane_of_arthropods", true)
                .bind(expandedBane)
                .comment("Should Blessing damage all hostile mobs, and deal extra damage to entities tagged as `minecraft:undead`.")
                .comment("  If set to false, only the entities in this tag will be damaged")
                .comment("  Non damaged entities are healed instead")
                .add("alternate_blessing", true)
                .bind(altBlessing)
                .comment("If true, Curse of Storytelling will have a cyan name. Only cosmetic")
                .add("alternate_storytelling_curse", false)
                .bind(altStorytelling)
                .comment("")
                .comment("Set these to false to disable the corresponding enchantment from appearing in game (disables at registry level)")
                .comment("  Only applies to Sortilege added enchantments")
                .addObject("enabled_enchants", enabled_enchants -> enabled_enchants
                        .add("potency", true)
                        .add("stability", true)
                        .add("chaining", true)
                        .add("focus", true)
                        .add("wisdom", true)
                        .add("push", true)
                        .add("pull", true)
                        .add("brazier", true)
                        .add("blizzard", true)
                        .add("blast", true)
                        .add("blitz", true)
                        .add("blessing", true)
                        .add("bonk", true)
                        .add("ignorance_curse", true)
                        .comment("")
                        .add("magic_protection", true)
                        .add("arcane", true)
                        .add("soulbound", true)
                        .add("storytelling_curse", true)
                )
                .bind(disabledEnchants)
        )
        .addObject("experience", experience -> experience
                .addObject("witch_hat", witch_hat -> witch_hat
                        .comment("Should the Witch Hat be registered as an item")
                        .add("enabled", true)
                        .bind(witchHatEnabled)
                        .comment("Chance for the Witch Hat to drop when killing a Witch. Set to 0 to disable the drop")
                        .add("drop_chance", 0.1)
                        .bind(witchHatDropChance)
                        .comment("How many extra experience points should drop when killing a monster with the Witch Hat equipped")
                        .add("xp_bonus", 3)
                        .bind(witchHatBonus)
                )
                .comment("")
                .comment("Should enchanting in an enchanting table cost more xp than the default 1 2 3 levels")
                .add("increased_enchants_cost", true)
                .bind(doIncreasedEnchantCosts)
                .comment("If the above is true, defines the new costs to replace 1 2 3")
                .addArray("costs", List.of(1, 3, 7))
                .bind(increasedEnchantCosts)
                .comment("If the above is true, defines the required xp levels to enchant")
                .addArray("needs", List.of(5, 10, 15))
                .bind(increasedEnchantNeeds)
                .comment("")
                .comment("Should Anvils never cost experience")
                .add("no_xp_anvil", true)
                .bind(noXPAnvil)
                .comment("Maximum experience level a player can have before it can't increase anymore. Set to -1 to disable the limit, and to 0 to disable experience")
                .add("level_cap", 100)
                .bind(xpLevelCap)
                .comment("How much xp points are needed to level up, in place of the exponential formula vanilla has")
                .comment("  Set to 0 or lower to use vanilla's formula")
                .add("linear_xp_requirement", 50)
                .bind(xpLinearCost)
                .comment("")
                .comment("Should monsters have a chance to give a bunch of extra experience points when killed")
                .addObject("bounties", bounties -> bounties
                        .comment("Should the sortilege:bounties tag act as a whitelist instead of a blacklist. It defines which mobs can drop bounties")
                        .add("tag_is_whitelist", false)
                        .bind(bountyTagWhitelist)
                        .comment("Amount of xp points bounties drop")
                        .add("value", 20)
                        .bind(bountyValue)
                        .comment("Chance for a bounty to happen")
                        .add("chance", 0.05)
                        .bind(bountyChance)
                )
                .comment("")
                .comment("Locks certain recipes behind experience levels or advancements.")
                .comment("  Each entry must be of the form \"modid:recipeid\": minimalxplevel or \"modid:recipeid\": \"advancementid\"")
                .comment("  The default config locks the crafting of Ender Eyes behind level 30 and the Beacon behind summoning the Wither, as an example")
                .addObject("recipe_locks", recipe_locks -> recipe_locks
                        .add("minecraft:ender_eye", 30)
                        .add("minecraft:beacon", "minecraft:nether/summon_wither")
                )
                .bind(recipeLocks)
        )
        .addObject("death", death -> death
                .addObject("xp_keeping", xp_keeping -> xp_keeping
                        .comment("Enable a balanced keepInventory only for experience")
                        .add("enable", true)
                        .bind(doXPKeep)
                        .comment("Ratio of xp kept on death")
                        .add("self_ratio", 0.3)
                        .bind(selfXPRatio)
                        .comment("Ratio of xp stolen by the attacker, and dropped back when it's killed")
                        .add("attacker_ratio", 0.6)
                        .bind(attackerXPRatio)
                        .comment("Ratio of xp dropped on the ground on death")
                        .add("drop_ratio", 0.1)
                        .bind(dropXPRatio)
                )
                .comment("Should the mob that killed you be made glowing")
                .add("glowing_killer", true)
                .bind(glowingKiller)
                .comment("")
                .comment("Keep equipped items (armor and hotbar) on death")
                .add("keep_equipped", false)
                .bind(keepEquipped)
                .comment("Should the Soulbound enchantment be removed on use")
                .add("consume_soulbound", false)
                .bind(consumeSoulbound)
                .comment("Display death coordinates instead of the score from vanilla on the death screen")
                .add("show_coordinates_on_death", true)
                .bind(showDeathCoordinates)
        )
        .addObject("brewing", brewing -> brewing
                .addObject("antidote", antidote -> antidote
                        .comment("Should Antidotes be registered as items")
                        .add("enable", true)
                        .bind(antidoteEnabled)
                        .comment("A list of potion effects for which Antidotes should not be registered")
                        .addArray("effect_blacklist", List.of())
                        .add("stack_size", 16)
                        .bind(antidoteStackSize)
                        .comment("For how many seconds Antidotes make you immune to their effect after drinking. Set to 0 or lower to disable extra immunity")
                        .add("immunity_time", 300)
                        .bind(antidoteImmunityTime)
                )
                .comment("")
                .addObject("potion", potion -> potion
                        .comment("Should potions have the ability to have different textures depending on their content.")
                        .comment("  Setting this to false will disable texture variants for long/strong potions, as well as per potion ones")
                        .add("custom_potion_textures", true)
                        .bind(customPotionTextures)
                        .comment("Should potions' properties be editable by datapack. Keep in mind that to fully disable modifications to potions,")
                        .comment("  You also need to set default_stack_size to 1, duration_multiplier to 1, default_use_time to 0 and default_cooldown to 0")
                        .add("custom_potion_data", true)
                        .bind(customPotionData)
                        .comment("")
                        .comment("Stack size for potions, for those lacking a custom datapack definition")
                        .add("default_stack_size", 8)
                        .bind(potionStackSize)
                        .comment("How many ticks should drinking a potion take. 20t = 1s")
                        .add("default_use_time", 20)
                        .bind(potionDrinkingTime)
                        .comment("How many ticks of cooldown potions get after being drunk or thrown")
                        .add("default_cooldown", 200)
                        .bind(potionCooldown)
                        .comment("Value to multiply all potions effects length by.")
                        .comment("  For example, if this is 2, then all effects gained *from potions* will last twice as long")
                        .comment("  This has no effect on potions whose effects were overridden by datapack")
                        .add("duration_multiplier", 1.5)
                        .bind(potionDurationMultiplier)
                        .comment("Should extra information like drinking time and cooldown be shown in a potion's tooltip")
                        .add("show_tooltip", true)
                        .bind(potionTooltip)
                )
                .comment("")
                .addObject("cauldron", cauldron -> cauldron
                        .comment("Should cauldron brewing be enabled")
                        .add("enable", true)
                        .bind(cauldronBrewingEnabled)
                        .comment("If true, cauldrons generated in Swamp Huts will have a random potion inside")
                        .add("fill_swamp_huts_randomly", true)
                        .bind(swampHutCauldrons)
                        .comment("A list of effect ids that are not allowed to generate in swamp huts")
                        .addArray("swamp_hut_blacklist", List.of())
                        .bind(swampHutBlacklist)
                        .comment("")
                        .comment("To disable cauldrons from filling over time automatically if above a soul campfire,")
                        .comment("  override the #sortilege:refills_cauldrons block tag to be empty")
                        .comment("Similarly, to disable the ability to refill cauldrons by throwing blaze powder in them,")
                        .comment("  override the #sortilege:refills_cauldrons item tag to be empty")
                )
        )
        .addObject("equipment", equipment -> equipment
                .addObject("lapis_shield", lapis_shield -> lapis_shield
                        .comment("Should the Lapis Shield be registered as an item")
                        .add("enable", true)
                        .bind(lapisShieldEnabled)
                        .comment("How many durability points do Lapis Shields have")
                        .add("durability", 152)
                        .bind(lapisShieldDurability)
                        .comment("How many ticks between each Lapis Shield dodge, in ticks (20t = 1s)")
                        .add("cooldown", 80)
                        .bind(lapisShieldCooldown)
                )
                .comment("Staffs are coming later")
        );
    }


    // Enchanting
    public static final ConfigEntry<Integer> enchantLimiterDefault = new ConfigEntry<>(3);
    public static final ConfigEntry<Boolean> cursesAddSlots = new ConfigEntry<>(true);
    public static final ConfigEntry<String> enchantLimiterMode = new ConfigEntry<>("relative");
    public static final ConfigEntry<Map<String, Integer>> enchantLimiterOverrides = new ConfigEntry<Map<String, Integer>>(Map.of()).withProcessor(json -> {
        Map<String, Integer> result = new HashMap<>();
        JsonObject obj = json.getAsJsonObject();
        for (String key : obj.keySet())
            result.put(key, obj.getAsJsonPrimitive(key).getAsInt());
        return result;
    });
    public static final ConfigEntry<Boolean> alwaysShowEnchantLimit = new ConfigEntry<>(true);
    public static final ConfigEntry<Boolean> limititeHasGlint = new ConfigEntry<>(true);
    public static final ConfigEntry<Integer> limititeLootWeight = new ConfigEntry<>(24);
    public static final ConfigEntry<Integer> maxLimitBreak = new ConfigEntry<>(3);
    public static final ConfigEntry<Boolean> catalystBooks = new ConfigEntry<>(true);
    public static final ConfigEntry<Double> catalystChance = new ConfigEntry<>(0.5);
    public static final ConfigEntry<Boolean> catalystTooltip = new ConfigEntry<>(true);
    public static final ConfigEntry<Boolean> catalystOnly = new ConfigEntry<>(false);
    public static final ConfigEntry<Boolean> knowledgeEnabled = new ConfigEntry<>(true);
    public static final ConfigEntry<Boolean> knowledgeTooltip = new ConfigEntry<>(true);
    public static final ConfigEntry<Boolean> allowInventoryEnchanting = new ConfigEntry<>(false);
    public static final ConfigEntry<Boolean> miningMasterIntegration = new ConfigEntry<>(true);
    public static final ConfigEntry<Boolean> magicProtCompatibility = new ConfigEntry<>(false);
    public static final ConfigEntry<Integer> expandedFeatherFalling = new ConfigEntry<>(4);
    public static final ConfigEntry<Integer> expandedUnbreaking = new ConfigEntry<>(3);
    public static final ConfigEntry<Boolean> expandedMagicProt = new ConfigEntry<>(true);
    public static final ConfigEntry<Integer> expandedFireProt = new ConfigEntry<>(4);
    public static final ConfigEntry<Boolean> expandedBane = new ConfigEntry<>(true);
    public static final ConfigEntry<Boolean> altBlessing = new ConfigEntry<>(true);
    public static final ConfigEntry<Boolean> altStorytelling = new ConfigEntry<>(false);
    public static final ConfigEntry<Set<String>> disabledEnchants = new ConfigEntry<Set<String>>(Set.of()).withProcessor(json -> {
        Set<String> result = new HashSet<>();
        JsonObject obj = json.getAsJsonObject();
        for (String key : obj.keySet())
            if (!obj.getAsJsonPrimitive(key).getAsBoolean())
                result.add(key);
        return result;
    });

    // Experience
    public static final ConfigEntry<Boolean> witchHatEnabled = new ConfigEntry<>(true);
    public static final ConfigEntry<Double> witchHatDropChance = new ConfigEntry<>(0.1);
    public static final ConfigEntry<Integer> witchHatBonus = new ConfigEntry<>(3);
    public static final ConfigEntry<Boolean> doIncreasedEnchantCosts = new ConfigEntry<>(true);
    public static final ConfigEntry<List<Integer>> increasedEnchantCosts = new ConfigEntry<>(List.of(1, 3, 7)).withProcessor(json -> {
        List<Integer> result = new ArrayList<>();
        for (JsonElement elm : json.getAsJsonArray())
           result.add(elm.getAsInt());
        return result;
    });
    public static final ConfigEntry<List<Integer>> increasedEnchantNeeds = new ConfigEntry<>(List.of(5, 10, 15)).withProcessor(json -> {
        List<Integer> result = new ArrayList<>();
        for (JsonElement elm : json.getAsJsonArray())
            result.add(elm.getAsInt());
        return result;
    });
    public static final ConfigEntry<Boolean> noXPAnvil = new ConfigEntry<>(true);
    public static final ConfigEntry<Integer> xpLevelCap = new ConfigEntry<>(100);
    public static final ConfigEntry<Integer> xpLinearCost = new ConfigEntry<>(50);
    public static final ConfigEntry<Boolean> bountyTagWhitelist = new ConfigEntry<>(false);
    public static final ConfigEntry<Integer> bountyValue = new ConfigEntry<>(20);
    public static final ConfigEntry<Double> bountyChance = new ConfigEntry<>(0.05);
    public static final ConfigEntry<Map<String, RecipeLock>> recipeLocks = new ConfigEntry<Map<String, RecipeLock>>(Map.of()).withProcessor(json -> {
        Map<String, RecipeLock> result = new HashMap<>();

        JsonObject obj = json.getAsJsonObject();
        for (String key : obj.keySet()) {
            JsonElement elm = obj.get(key);

            if (elm.isJsonPrimitive() && elm.getAsJsonPrimitive().isNumber())
                result.put(key, new RecipeLock.LevelLock(elm.getAsInt()));
            else if (elm.isJsonPrimitive() && elm.getAsJsonPrimitive().isString())
                result.put(key, new RecipeLock.AdvancementLock(elm.getAsString()));
        }

        return result;
    });

    // Death
    public static final ConfigEntry<Boolean> doXPKeep = new ConfigEntry<>(true);
    public static final ConfigEntry<Double> selfXPRatio = new ConfigEntry<>(0.3);
    public static final ConfigEntry<Double> attackerXPRatio = new ConfigEntry<>(0.6);
    public static final ConfigEntry<Double> dropXPRatio = new ConfigEntry<>(0.1);
    public static final ConfigEntry<Boolean> keepEquipped = new ConfigEntry<>(false);
    public static final ConfigEntry<Boolean> consumeSoulbound = new ConfigEntry<>(false);
    public static final ConfigEntry<Boolean> showDeathCoordinates = new ConfigEntry<>(true);
    public static final ConfigEntry<Boolean> glowingKiller = new ConfigEntry<>(true);

    // Brewing
    public static final ConfigEntry<Boolean> antidoteEnabled = new ConfigEntry<>(true);
    public static final ConfigEntry<Set<ResourceLocation>> antidoteBlacklist = new ConfigEntry<Set<ResourceLocation>>(Set.of()).withProcessor(json -> {
        Set<ResourceLocation> result = new HashSet<>();
        for (JsonElement elm : json.getAsJsonArray())
            result.add(Identifier.of(elm.getAsString()));
        return result;
    });
    public static final ConfigEntry<Integer> antidoteStackSize = new ConfigEntry<>(16);
    public static final ConfigEntry<Integer> antidoteImmunityTime = new ConfigEntry<>(300);
    public static final ConfigEntry<Integer> potionStackSize = new ConfigEntry<>(8);
    public static final ConfigEntry<Double> potionDurationMultiplier = new ConfigEntry<>(1.5);
    public static final ConfigEntry<Integer> potionDrinkingTime = new ConfigEntry<>(20);
    public static final ConfigEntry<Integer> potionCooldown = new ConfigEntry<>(200);
    public static final ConfigEntry<Boolean> potionTooltip = new ConfigEntry<>(true);
    public static final ConfigEntry<Boolean> customPotionTextures = new ConfigEntry<>(true);
    public static final ConfigEntry<Boolean> customPotionData = new ConfigEntry<>(true);
    public static final ConfigEntry<Boolean> cauldronBrewingEnabled = new ConfigEntry<>(true);
    public static final ConfigEntry<Boolean> swampHutCauldrons = new ConfigEntry<>(true);
    public static final ConfigEntry<Set<ResourceLocation>> swampHutBlacklist = new ConfigEntry<Set<ResourceLocation>>(Set.of()).withProcessor(json -> {
        Set<ResourceLocation> result = new HashSet<>();
        for (JsonElement elm : json.getAsJsonArray())
            result.add(Identifier.of(elm.getAsString()));
        return result;
    });

    // Equipment
    public static final ConfigEntry<Boolean> lapisShieldEnabled = new ConfigEntry<>(true);
    public static final ConfigEntry<Integer> lapisShieldDurability = new ConfigEntry<>(152);
    public static final ConfigEntry<Integer> lapisShieldCooldown = new ConfigEntry<>(80);


    // Staff
    public static void buildStaffs(IJsonBuilder builder) {
        builder.addObject("overcharge", overcharge -> overcharge
                .comment("Default overcharge configs, unless custom behavior is set for a staff")
                .comment("  by following the same structure in `cost.overcharge`")
                .comment("")
                .comment("Maximal overcharge a staff can hold at any given time")
                .add("max", 20)
                .comment("Color for the overcharge bar. Hexadecimal format")
                .add("bar_color", "0x0000ff")
                .comment("Should overcharged staffs ignore durability cost when firing")
                .add("ignore_durability", true)
                .comment("Should overcharged staffs ignore resource cost when firing")
                .add("ignore_cost", true)
                .comment("Which items can be used to overcharge staffs. Must be formatted as \"modid:itemid\": value")
                .addObject("ingredients", ingredients -> ingredients
                        .add("minecraft:lapis_lazuli", 2)
                        .add("minecraft:lapis_block", 20)
                )
        )
        .bind(defaultOvercharge)
        .addObject("behavior", behavior -> behavior
                .comment("If true, staff beams will pass through blocks up to their pierce value")
                .add("pierce_blocks", false)
                .bind(staffsPierceBlocks)
        )
        .comment("")
        .comment("ENTRIES")
        .addArray("entries", entries -> entries
                .addObject(staff -> staff
                        .add("id", "golden_staff")
                        .add("sort_index", 40)
                        .add("type", "sortilege:experience")
                        .add("dependency", "minecraft")
                        .comment("")
                        .addObject("properties", properties -> properties
                                .add("parent", "GOLD")
                                .comment("")
                                .add("fireproof", false)
                                .add("durability", 112)
                                .add("repair_material", "minecraft:gold_ingot")
                                .add("enchantability", 18)
                                .comment("")
                                .add("damage", 3)
                                .add("piercing", 2)
                                .add("range", 12)
                                .add("charge_time", 1)
                                .add("cooldown", 20)
                        )
                        .comment("")
                        .addObject("cost", cost -> cost
                                .addObject("overcharge", overcharge -> {})
                                .add("value", 3)
                        )
                        .comment("")
                        .addObject("effects", effects -> effects
                                .add("on_shoot", "")
                                .add("on_hit_self", "")
                                .add("on_hit_target", "")
                                .comment("")
                                .addObject("enchants", enchants -> {})
                        )
                        .comment("")
                        .addObject("display", display -> display
                                .add("particle", "sortilege:wisp")
                                .add("sound", "minecraft:amethyst")
                                .addArray("colors", List.of())
                        )
                        .comment("")
                        .addArray("recipes", recipes -> {})
                )
        ).bind(staffs);
    }


    public static final ConfigEntry<OverchargeEntry> defaultOvercharge = new ConfigEntry<>(new OverchargeEntry())
            .withProcessor(json -> OverchargeEntry.read(json.getAsJsonObject()));
    public static final ConfigEntry<Boolean> staffsPierceBlocks = new ConfigEntry<>(false);
    public static final ConfigEntry<List<StaffEntry>> staffs = new ConfigEntry<List<StaffEntry>>(List.of()).withProcessor(json -> {
        List<StaffEntry> result = new ArrayList<>();
        if (!json.isJsonArray()) return result;

        Set<ResourceLocation> disabledRecipes = new HashSet<>();
        Runnable remover = () -> {
            for (ResourceLocation recipe : disabledRecipes) {
                SolRegistries.Data.RUNTIME.addRemoval(
                        Sortilege.log().error(Identifier.of(recipe.getNamespace(), "recipes/" + recipe.getPath() + ".json")), () -> true);
            }
        };

        for (JsonElement it : json.getAsJsonArray()) {
            if (!it.isJsonObject()) continue;

            disabledRecipes.clear();
            try {
                StaffEntry entry = StaffEntry.read(it.getAsJsonObject(), disabledRecipes);
                if (entry == null) remover.run();
                else result.add(entry);
            } catch (JsonSyntaxException e) {
                Sortilege.log().error("Failed to read Staff entry: " + e);
                remover.run();
            }
        }

        result.sort(Comparator.comparingInt(StaffEntry::getSortIndex));
        return result;
    });
}
