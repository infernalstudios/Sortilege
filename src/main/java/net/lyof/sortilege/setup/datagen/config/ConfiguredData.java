package net.lyof.sortilege.setup.datagen.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.enchant.ModEnchants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConfiguredData {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public final ResourceLocation target;
    public Function<JsonElement, String> provider;
    public final Supplier<Boolean> enabled;

    public ConfiguredData(ResourceLocation target, Supplier<Boolean> enabled, Function<JsonElement, String> provider) {
        this.target = target;
        this.provider = provider;
        this.enabled = enabled;
    }

    public String apply(@Nullable String original) {
        return gson.fromJson(this.provider.apply(gson.fromJson(original == null ? "" : original, JsonElement.class)),
                JsonElement.class).toString();
    }


    public static List<ConfiguredData> INSTANCES = new LinkedList<>();

    public static @Nullable ConfiguredData get(ResourceLocation id) {
        return INSTANCES.stream().filter(data -> data.target.equals(id)).findAny().orElse(null);
    }

    protected static void register(ResourceLocation target, Supplier<Boolean> enabled, Function<JsonElement, String> provider) {
        INSTANCES.add(new ConfiguredData(target, enabled, provider));
    }

    protected static void registerMiningMaster(String gem, Enchantment enchant) {
        register(ResourceLocation.tryBuild("miningmaster", "recipes/smithing/" + gem + "_smithing.json"),
                () -> enchant != null && ModConfig.miningMasterIntegration.get(),
                json -> Common.changeMiningMasterGem(json, BuiltInRegistries.ENCHANTMENT.getKey(enchant).toString()));
        register(ResourceLocation.tryBuild("sortilege", "recipes/catalyst/" + gem + ".json"),
                () -> enchant != null && ModConfig.miningMasterIntegration.get(),
                json -> Common.changeMiningMasterGem(json, BuiltInRegistries.ENCHANTMENT.getKey(enchant).toString()));
    }


    public static void register() {
        register(Sortilege.MOD.makeID("tags/items/staffs.json"), () -> true, Common::generateStaffTag);
        register(ResourceLocation.tryBuild("minecraft", "advancements/adventure/voluntary_exile.json"),
                () -> ModConfig.witchHatEnabled.get(), Common::changeVoluntaryExileParent);

        if (FabricLoader.getInstance().isModLoaded("miningmaster")) {
            registerMiningMaster("power_pyrite", ModEnchants.POTENCY);
            registerMiningMaster("kinetic_opal", ModEnchants.BLAST);
            registerMiningMaster("ice_sapphire", ModEnchants.BLIZZARD);
            registerMiningMaster("fire_ruby", ModEnchants.BRAZIER);
            registerMiningMaster("air_malachite", ModEnchants.BLITZ);
            registerMiningMaster("spirit_garnet", ModEnchants.WISDOM);
            registerMiningMaster("haste_peridot", ModEnchants.FOCUS);
            registerMiningMaster("divine_beryl", ModEnchants.BLESSING);
        }
    }

    public static void registerClient() {
        /*for (Pair<String, ModConfigS.StaffInfo> staff : ModConfigS.STAFFS)
            register(Sortilege.MOD.makeID("models/item/" + staff.getFirst() + ".json"),
                    () -> FabricLoader.getInstance().isModLoaded(staff.getSecond().dependency),
                    json -> Client.generateDefaultModel(json, staff.getFirst()));*/

        register(Sortilege.MOD.makeID("lang/en_us.json"), () -> true, Client::generateTranslations);

        register(ResourceLocation.tryBuild("enchdesc", "lang/en_us.json"),
                () -> FabricLoader.getInstance().isModLoaded("enchdesc"),
                Client::changeEnchantmentDescriptions);

        register(ResourceLocation.tryBuild("quark", "attribute_tooltips.json"),
                () -> FabricLoader.getInstance().isModLoaded("quark"),
                Client::changeQuarkAttributeDisplay);
    }

    protected static JsonElement getJson(String string) {
        return gson.fromJson(string, JsonElement.class);
    }


    private static class Common {
        public static String generateStaffTag(JsonElement json) {
            if (json == null) json = new JsonObject();
            json.getAsJsonObject().add("values", new JsonArray());

            /*for (Pair<String, ModConfigS.StaffInfo> staff : ModConfigS.STAFFS) {
                if (!FabricLoader.getInstance().isModLoaded(staff.getSecond().dependency)) continue;
                json.getAsJsonObject().get("values").getAsJsonArray().add(Sortilege.MOD.makeID(staff.getFirst()).toString());
            }*/
            return json.toString();
        }

        public static String changeVoluntaryExileParent(JsonElement json) {
            json.getAsJsonObject().asMap().replace("parent", new JsonPrimitive(Sortilege.MOD.makeID("get_witch_hat").toString()));
            return json.toString();
        }

        public static String changeMiningMasterGem(JsonElement json, String addedEnchant) {
            if (json == null) return "{}";
            if (!json.isJsonObject() || !json.getAsJsonObject().has("enchantments")
                    || !json.getAsJsonObject().get("enchantments").isJsonArray()) return json.toString();

            json.getAsJsonObject().get("enchantments").getAsJsonArray().add(addedEnchant);
            return json.toString();
        }
    }


    public static class Client {
        public static String generateDefaultModel(JsonElement json, String path) {
            if (json != null) return json.toString();

            return """
                    {
                      "parent": "item/handheld",
                      "textures": {
                        "layer0": """ + "\"sortilege:item/" + path + "\"" + """
                      }
                    }""";
        }

        public static String generateTranslations(JsonElement json) {
            if (json == null) json = new JsonObject();
            JsonObject o = (JsonObject) json;

            /*for (Pair<String, ModConfigS.StaffInfo> staff : ModConfigS.STAFFS) {
                if (!FabricLoader.getInstance().isModLoaded(staff.getSecond().dependency)) continue;

                String id = staff.getFirst();
                if (o.has("item." + Sortilege.MOD_ID + "." + id)) continue;

                StringBuilder translation = new StringBuilder(id.toUpperCase().charAt(0) + "");
                for (int i = 1; i < id.length(); i++) {
                    if (id.charAt(i - 1) == '_')
                        translation.append(id.toUpperCase().charAt(i));
                    else if (id.charAt(i) == '_')
                        translation.append(' ');
                    else
                        translation.append(id.charAt(i));
                }
                o.addProperty("item." + Sortilege.MOD_ID + "." + id, translation.toString());
            }
*/
            if (ModConfig.betterMagicProt.get())
                o.asMap().replace("enchantment.sortilege.magic_protection.desc",
                        new JsonPrimitive("Reduces damage from magic, and gives a chance to dodge attacks."));

            return o.toString();
        }

        public static String changeEnchantmentDescriptions(JsonElement json) {
            if (json == null) return "{}";
            JsonObject o = (JsonObject) json;

            if (ModConfig.betterFireProt.get() > 0)
                o.asMap().replace("enchantment.minecraft.fire_protection.desc",
                        new JsonPrimitive(o.get("enchantment.minecraft.fire_protection.desc").getAsString()
                                + " Wearing a full set at max level completely negates them."));
            if (ModConfig.betterBane.get())
                o.asMap().replace("enchantment.minecraft.bane_of_arthropods.desc",
                        new JsonPrimitive(o.get("enchantment.minecraft.bane_of_arthropods.desc").getAsString()
                                + " Also slows down opponents."));
            if (ModConfig.betterFeatherFalling.get() > 0)
                o.asMap().replace("enchantment.minecraft.feather_falling.desc",
                        new JsonPrimitive(o.get("enchantment.minecraft.feather_falling.desc").getAsString()
                                + " They are completely negated at max level."));
            if (ModConfig.betterUnbreaking.get() > 0)
                o.asMap().replace("enchantment.minecraft.unbreaking.desc",
                        new JsonPrimitive(o.get("enchantment.minecraft.unbreaking.desc").getAsString()
                                + " Max level makes the item unbreakable"));

            return o.toString();
        }

        public static String changeQuarkAttributeDisplay(JsonElement json) {
            if (json == null) return "";
            if (!json.isJsonObject()) return "";

            json.getAsJsonObject().add("sortilege:generic.staff_damage", getJson("""
                    {
                      "display": {
                        "mainhand": "flat",
                        "offhand": "flat",
                        "feet": "difference",
                        "legs": "difference",
                        "chest": "difference",
                        "head": "difference",
                        "potion": "difference"
                      },
                      "texture": "quark:attribute/staff_damage",
                      "compare": "higher_better"
                    }"""));
            json.getAsJsonObject().add("sortilege:generic.staff_range", getJson("""
                    {
                      "display": {
                        "mainhand": "flat",
                        "offhand": "flat",
                        "feet": "difference",
                        "legs": "difference",
                        "chest": "difference",
                        "head": "difference",
                        "potion": "difference"
                      },
                      "texture": "quark:attribute/staff_range",
                      "compare": "higher_better"
                    }"""));
            json.getAsJsonObject().add("sortilege:generic.staff_pierce", getJson("""
                    {
                      "display": {
                        "mainhand": "flat",
                        "offhand": "flat",
                        "feet": "difference",
                        "legs": "difference",
                        "chest": "difference",
                        "head": "difference",
                        "potion": "difference"
                      },
                      "texture": "quark:attribute/staff_pierce",
                      "compare": "higher_better"
                    }"""));

            return gson.toJson(json);
        }
    }
}
