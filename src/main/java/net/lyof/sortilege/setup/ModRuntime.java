package net.lyof.sortilege.setup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.fabricmc.loader.api.FabricLoader;
import net.lcc.sollib.api.common.SolRegistries;
import net.lcc.sollib.api.common.config.builder.JsonBuilder;
import net.lcc.sollib.core.Identifier;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModRuntime {
    protected static void addMiningMaster(String gem, Enchantment enchant) {
        SolRegistries.Data.RUNTIME.addJson(Identifier.of("miningmaster", "recipes/smithing/" + gem + "_smithing.json"),
                json -> ModRuntime.Common.changeMiningMasterGem(json, BuiltInRegistries.ENCHANTMENT.getKey(enchant).toString()),
                () -> enchant != null && ModConfig.miningMasterIntegration.get());
        SolRegistries.Data.RUNTIME.addJson(Sortilege.MOD.makeID("recipes/gems/" + gem + ".json"),
                json -> ModRuntime.Common.changeMiningMasterGem(json, BuiltInRegistries.ENCHANTMENT.getKey(enchant).toString()),
                () -> enchant != null && ModConfig.miningMasterIntegration.get());
    }


    public static void load() {
        SolRegistries.Data.RUNTIME.addJson(Sortilege.MOD.makeID("tags/items/staffs.json"),
                ModRuntime.Common::changeStaffTag);
        SolRegistries.Data.RUNTIME.addJson(Identifier.of("minecraft", "advancements/adventure/voluntary_exile.json"),
                ModRuntime.Common::changeVoluntaryExileParent, ModConfig.witchHatEnabled);

        if (FabricLoader.getInstance().isModLoaded("miningmaster")) {
            addMiningMaster("power_pyrite", ModEnchants.POTENCY);
            addMiningMaster("kinetic_opal", ModEnchants.BLAST);
            addMiningMaster("ice_sapphire", ModEnchants.BLIZZARD);
            addMiningMaster("fire_ruby", ModEnchants.BRAZIER);
            addMiningMaster("air_malachite", ModEnchants.BLITZ);
            addMiningMaster("spirit_garnet", ModEnchants.WISDOM);
            addMiningMaster("haste_peridot", ModEnchants.FOCUS);
            addMiningMaster("divine_beryl", ModEnchants.BLESSING);
        }
    }

    public static void loadClient() {
        for (StaffEntry entry : ModConfig.staffs.get())
            SolRegistries.Data.RUNTIME.addJson(Sortilege.MOD.makeID("models/item/" + entry.getID() + ".json"),
                    json -> Client.generateDefaultModel(json, entry.getID()));

        SolRegistries.Data.RUNTIME.addJson(Sortilege.MOD.makeID("lang/en_us.json"), ModRuntime.Client::generateTranslations);

        SolRegistries.Data.RUNTIME.addJson(Identifier.of("enchdesc", "lang/en_us.json"),
                ModRuntime.Client::changeEnchantmentDescriptions,
                () -> FabricLoader.getInstance().isModLoaded("enchdesc"));

        SolRegistries.Data.RUNTIME.addJson(Identifier.of("quark", "attribute_tooltips.json"),
                ModRuntime.Client::changeQuarkAttributeDisplay,
                () -> FabricLoader.getInstance().isModLoaded("quark"));
    }


    private static class Common {
        public static JsonObject changeStaffTag(JsonObject json) {
            if (json == null) json = new JsonObject();
            json.add("values", new JsonArray());

            for (StaffEntry entry : ModConfig.staffs.get())
                json.get("values").getAsJsonArray().add(Sortilege.MOD.makeID(entry.getID()).toString());
            return json;
        }

        public static JsonObject changeVoluntaryExileParent(JsonObject json) {
            json.asMap().replace("parent", new JsonPrimitive(Sortilege.MOD.makeID("get_witch_hat").toString()));
            return json;
        }

        public static JsonObject changeMiningMasterGem(JsonObject json, String addedEnchant) {
            if (json == null) return null;
            if (!json.has("enchantments") || !json.get("enchantments").isJsonArray()) return json;

            json.getAsJsonArray("enchantments").add(addedEnchant);
            return json;
        }
    }


    public static class Client {
        public static JsonObject generateDefaultModel(JsonObject json, String path) {
            if (json != null) return json;

            return new JsonBuilder().add("parent", "item/handheld")
                    .addObject("textures", textures -> textures
                            .add("layer0", "sortilege:item/" + path)
                    ).toJson();
        }

        public static JsonObject generateTranslations(JsonObject json) {
            if (json == null) json = new JsonObject();

            for (StaffEntry entry : ModConfig.staffs.get()) {
                String id = entry.getID();
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
