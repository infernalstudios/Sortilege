package net.lyof.sortilege.setup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.fabricmc.loader.api.FabricLoader;
import net.lcc.sollib.SolLib;
import net.lcc.sollib.api.common.SolRegistries;
import net.lcc.sollib.api.common.config.builder.JsonBuilder;
import net.lcc.sollib.core.Identifier;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class ModRuntime {
    protected static void addMiningMaster(String gem, String enchant) {
        SolRegistries.Data.RUNTIME.addJson(Identifier.of("miningmaster:recipes/smithing/" + gem + "_smithing.json"),
                json -> ModRuntime.Common.changeMiningMasterGem(json, Sortilege.MOD_ID + ":" + enchant),
                () -> ModConfig.enabledEnchants.get().contains(enchant) && ModConfig.miningMasterIntegration.get());
        SolRegistries.Data.RUNTIME.addJson(Sortilege.MOD.makeID("recipes/gems/" + gem + ".json"),
                json -> ModRuntime.Common.changeMiningMasterGem(json, Sortilege.MOD_ID + ":" + enchant),
                () -> ModConfig.enabledEnchants.get().contains(enchant) && ModConfig.miningMasterIntegration.get());
    }


    public static void load() {
        SolRegistries.Data.RUNTIME.addJson(SolLib.MOD.makeID("tags/entity_type/generated/friendly.json"),
                Common::sol_generateFriendlyTag);

        SolRegistries.Data.RUNTIME.addJson(Sortilege.MOD.makeID("tags/item/staffs.json"),
                Common::generateStaffTag);

        SolRegistries.Data.RUNTIME.addJson(Identifier.of("minecraft:advancements/adventure/voluntary_exile.json"),
                json -> Common.changeParent(json, "sortilege:get_witch_hat"), ModConfig.witchHatEnabled);
        SolRegistries.Data.RUNTIME.addJson(Identifier.of("minecraft:advancements/story/enchant_item.json"),
                json -> Common.changeParent(json, "sortilege:get_knowledge_book"), ModConfig.knowledgeEnabled);

        //#region Expanded Enchantments
        SolRegistries.Data.RUNTIME.addJson(Identifier.of("minecraft:enchantment/unbreaking.json"),
                json -> Common.changeEffects(json, new JsonBuilder()
                        .add("sortilege:true_unbreaking", ModConfig.expandedUnbreaking.get())
                        .toJson()),
                () -> ModConfig.expandedUnbreaking.get() > -1);
        SolRegistries.Data.RUNTIME.addJson(Identifier.of("minecraft:enchantment/feather_falling.json"),
                json -> Common.changeEffects(json, new JsonBuilder()
                        .addArray("sortilege:dodge_chance", dodge -> dodge.addObject(main -> main
                            .addObject("effect", effect -> effect
                                .add("type", "minecraft:add")
                                .add("value", 1)
                            ).addObject("requirements", requirements -> requirements
                                .add("condition", "minecraft:all_of")
                                .addArray("terms", terms -> terms
                                    .addObject(level -> level
                                        .add("condition", "minecraft:value_check")
                                        .addObject("value", value -> value
                                            .add("type", "minecraft:enchantment_level")
                                            .addObject("amount", amount -> amount
                                                .add("type", "minecraft:linear")
                                                .add("base", 1)
                                                .add("per_level_above_first", 1)
                                            )
                                        ).addObject("range", range -> range
                                            .add("min", ModConfig.expandedFeatherFalling.get())
                                        )
                                    ).addObject(source -> source
                                        .add("condition", "minecraft:damage_source_properties")
                                        .addObject("predicate", predicate -> predicate
                                            .addArray("tags", tags -> tags
                                                .addObject(tag -> tag
                                                    .add("expected", true)
                                                    .add("id", "minecraft:is_fall")
                                                ).addObject(tag -> tag
                                                    .add("expected", false)
                                                    .add("id", "minecraft:bypasses_invulnerability")
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )).toJson()),
                () -> ModConfig.expandedFeatherFalling.get() > -1);
        SolRegistries.Data.RUNTIME.addJson(Identifier.of("minecraft:enchantment/fire_protection.json"),
                json -> Common.changeEffects(json, new JsonBuilder()
                        .add("sortilege:true_fire_protection", ModConfig.expandedFireProt.get())
                        .toJson()),
                () -> ModConfig.expandedFireProt.get() > -1);
        SolRegistries.Data.RUNTIME.addJson(Sortilege.MOD.makeID("enchantment/magic_protection.json"),
                json -> Common.changeEffects(json, new JsonBuilder()
                        .addArray("sortilege:dodge_chance", dodge -> dodge.addObject(main -> main
                            .addObject("effect", effect -> effect
                                .add("type", "minecraft:add")
                                .addObject("value", value -> value
                                    .add("type", "minecraft:linear")
                                    .add("base", 0.05)
                                    .add("per_level_above_first", 0.05)
                                )
                            )
                        )).toJson()),
                ModConfig.expandedMagicProt);
        SolRegistries.Data.RUNTIME.addJson(Identifier.of("minecraft:enchantment/bane_of_arthropods.json"),
                json -> Common.changeEffects(json, new JsonBuilder()
                        .addArray("minecraft:post_attack", hurt -> hurt.addObject(main -> main
                            .add("affected","victim")
                            .add("enchanted", "attacker")
                            .addObject("effect", effect -> effect
                                .add("type", "minecraft:apply_mob_effect")
                                .addObject("min_duration", value -> value
                                    .add("type", "minecraft:linear")
                                    .add("base", 0.75)
                                    .add("per_level_above_first", 0.75)
                                ).addObject("max_duration", value -> value
                                    .add("type", "minecraft:linear")
                                    .add("base", 0.75)
                                    .add("per_level_above_first", 0.75)
                                ).add("min_amplifier", 1)
                                .add("max_amplifier", 1)
                                .add("to_apply", "minecraft:slowness")
                            )
                        )).toJson()),
                ModConfig.expandedBane);
        //#endregion

        if (FabricLoader.getInstance().isModLoaded("miningmaster")) {
            addMiningMaster("power_pyrite", "potency");
            addMiningMaster("kinetic_opal", "blast");
            addMiningMaster("ice_sapphire", "blizzard");
            addMiningMaster("fire_ruby", "brazier");
            addMiningMaster("air_malachite", "blitz");
            addMiningMaster("spirit_garnet", "wisdom");
            addMiningMaster("haste_peridot", "focus");
            addMiningMaster("divine_beryl", "blessing");
        }
    }

    public static void loadClient() {
        for (AStaffItem staff : ModItems.STAFFS)
            SolRegistries.Data.RUNTIME.addJson(Sortilege.MOD.makeID("models/item/" + staff.getName() + ".json"),
                    json -> Client.generateDefaultModel(json, staff.getEntry().getID()));

        SolRegistries.Data.RUNTIME.addJson(Sortilege.MOD.makeID("lang/en_us.json"), Client::generateTranslations);

        SolRegistries.Data.RUNTIME.addJson(Identifier.of("enchdesc", "lang/en_us.json"),
                Client::changeEnchantmentDescriptions,
                () -> FabricLoader.getInstance().isModLoaded("enchdesc"));

        SolRegistries.Data.RUNTIME.addJson(Identifier.of("quark", "attribute_tooltips.json"),
                Client::changeQuarkAttributeDisplay,
                () -> FabricLoader.getInstance().isModLoaded("quark"));
    }


    private static class Common {
        public static JsonObject sol_generateFriendlyTag(JsonObject json) {
            return new JsonBuilder().addArray("values", values -> {
                for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE)
                    if (type.getCategory().isFriendly() && type.getCategory() != MobCategory.MISC)
                        values.add(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
            }).toJson();
        }

        public static JsonObject generateStaffTag(JsonObject json) {
            if (json == null) json = new JsonObject();
            json.add("values", new JsonArray());

            for (AStaffItem staff : ModItems.STAFFS)
                json.get("values").getAsJsonArray().add(Sortilege.MOD.makeID(staff.getName()).toString());
            return json;
        }

        public static JsonObject changeParent(JsonObject json, String parent) {
            if (json == null) return null;
            json.asMap().replace("parent", new JsonPrimitive(parent));
            return json;
        }

        public static JsonObject changeMiningMasterGem(JsonObject json, String addedEnchant) {
            if (json == null) return null;
            if (!json.has("enchantments") || !json.get("enchantments").isJsonArray()) return json;

            json.getAsJsonArray("enchantments").add(addedEnchant);
            return json;
        }

        public static JsonObject changeEffects(JsonObject json, JsonObject effects) {
            JsonObject e = GsonHelper.getAsJsonObject(json, "effects", new JsonObject());
            for (String key : effects.keySet()) {
                if (e.has(key) && e.get(key).isJsonArray()) {
                    effects.getAsJsonArray(key).forEach(e.getAsJsonArray(key)::add);
                } else e.add(key, effects.get(key));
            }

            json.add("effects", e);
            return json;
        }
    }


    private static class Client {
        public static JsonObject generateDefaultModel(JsonObject json, String path) {
            if (json != null) return json;

            return new JsonBuilder().add("parent", "item/handheld")
                    .addObject("textures", textures -> textures
                            .add("layer0", "sortilege:item/" + path)
                    ).toJson();
        }

        public static JsonObject generateTranslations(JsonObject json) {
            if (json == null) json = new JsonObject();

            for (AStaffItem staff : ModItems.STAFFS) {
                String id = staff.getName();
                if (json.has("item." + Sortilege.MOD_ID + "." + id)) continue;

                StringBuilder translation = new StringBuilder(id.toUpperCase().charAt(0) + "");
                for (int i = 1; i < id.length(); i++) {
                    if (id.charAt(i - 1) == '_')
                        translation.append(id.toUpperCase().charAt(i));
                    else if (id.charAt(i) == '_')
                        translation.append(' ');
                    else
                        translation.append(id.charAt(i));
                }
                json.addProperty("item." + Sortilege.MOD_ID + "." + id, translation.toString());
            }

            if (ModConfig.expandedMagicProt.get())
                json.asMap().replace("enchantment.sortilege.magic_protection.desc",
                        new JsonPrimitive("Reduces damage from magic, and gives a chance to dodge attacks."));

            return json;
        }

        public static JsonObject changeEnchantmentDescriptions(JsonObject json) {
            if (json == null) return null;

            if (ModConfig.expandedFireProt.get() > 0)
                json.asMap().replace("enchantment.minecraft.fire_protection.desc",
                        new JsonPrimitive(json.get("enchantment.minecraft.fire_protection.desc").getAsString()
                                + " Wearing a full set at max level completely negates them."));
            if (ModConfig.expandedBane.get())
                json.asMap().replace("enchantment.minecraft.bane_of_arthropods.desc",
                        new JsonPrimitive(json.get("enchantment.minecraft.bane_of_arthropods.desc").getAsString()
                                + " Also slows down opponents."));
            if (ModConfig.expandedFeatherFalling.get() > 0)
                json.asMap().replace("enchantment.minecraft.feather_falling.desc",
                        new JsonPrimitive(json.get("enchantment.minecraft.feather_falling.desc").getAsString()
                                + " They are completely negated at max level."));
            if (ModConfig.expandedUnbreaking.get() > 0)
                json.asMap().replace("enchantment.minecraft.unbreaking.desc",
                        new JsonPrimitive(json.get("enchantment.minecraft.unbreaking.desc").getAsString()
                                + " Max level makes the item unbreakable"));

            return json;
        }

        public static JsonObject changeQuarkAttributeDisplay(JsonObject json) {
            if (json == null) return null;

            json.add("sortilege:generic.staff_damage",
                    new JsonBuilder().addObject("display", display -> display
                            .add("mainhand", "flat")
                            .add("offhand", "flat")
                            .add("feet", "difference")
                            .add("legs", "difference")
                            .add("chest", "difference")
                            .add("head", "difference")
                            .add("potion", "difference")
                    )
                    .add("texture", "quark:attribute/staff_damage")
                    .add("compare", "higher_better").toJson()
            );
            json.add("sortilege:generic.staff_range", new JsonBuilder().addObject("display", display -> display
                            .add("mainhand", "flat")
                            .add("offhand", "flat")
                            .add("feet", "difference")
                            .add("legs", "difference")
                            .add("chest", "difference")
                            .add("head", "difference")
                            .add("potion", "difference")
                    )
                    .add("texture", "quark:attribute/staff_damage")
                    .add("compare", "higher_better").toJson()
            );
            json.add("sortilege:generic.staff_pierce", new JsonBuilder().addObject("display", display -> display
                            .add("mainhand", "flat")
                            .add("offhand", "flat")
                            .add("feet", "difference")
                            .add("legs", "difference")
                            .add("chest", "difference")
                            .add("head", "difference")
                            .add("potion", "difference")
                    )
                    .add("texture", "quark:attribute/staff_damage")
                    .add("compare", "higher_better").toJson()
            );

            return json;
        }
    }
}
