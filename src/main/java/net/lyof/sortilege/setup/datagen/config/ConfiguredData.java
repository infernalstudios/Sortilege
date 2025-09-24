package net.lyof.sortilege.setup.datagen.config;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.loader.api.FabricLoader;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.config.ModConfig;
import net.lyof.sortilege.enchant.ModEnchants;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConfiguredData {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public final Identifier target;
    public Function<JsonElement, String> provider;
    public final Supplier<Boolean> enabled;

    public ConfiguredData(Identifier target, Supplier<Boolean> enabled, Function<JsonElement, String> provider) {
        this.target = target;
        this.provider = provider;
        this.enabled = enabled;
    }

    public String apply(@Nullable String original) {
        return gson.fromJson(this.provider.apply(gson.fromJson(original == null ? "" : original, JsonElement.class)),
                JsonElement.class).toString();
    }


    public static List<ConfiguredData> INSTANCES = new LinkedList<>();

    public static @Nullable ConfiguredData get(Identifier id) {
        return INSTANCES.stream().filter(data -> data.target.equals(id)).findAny().orElse(null);
    }

    protected static void register(Identifier target, Supplier<Boolean> enabled, Function<JsonElement, String> provider) {
        INSTANCES.add(new ConfiguredData(target, enabled, provider));
    }


    public static void register() {
        register(Sortilege.makeID("tags/items/staffs.json"), () -> true, Common::generateStaffTag);
        register(Identifier.of("minecraft", "advancements/adventure/voluntary_exile.json"),
                () -> ConfigEntries.witchHatEnabled, Common::changeVoluntaryExileParent);

        if (FabricLoader.getInstance().isModLoaded("miningmaster")) {
            register(Identifier.of("miningmaster", "recipes/smithing/power_pyrite_smithing.json"),
                    () -> ModEnchants.POTENCY != null && ConfigEntries.miningMasterIntegration,
                    json -> Common.changeMiningMasterGem(json, "sortilege:potency"));
            register(Identifier.of("miningmaster", "recipes/smithing/kinetic_opal_smithing.json"),
                    () -> ModEnchants.PUSH != null && ConfigEntries.miningMasterIntegration,
                    json -> Common.changeMiningMasterGem(json, "sortilege:push"));
            register(Identifier.of("miningmaster", "recipes/smithing/ice_sapphire_smithing.json"),
                    () -> ModEnchants.BLIZZARD != null && ConfigEntries.miningMasterIntegration,
                    json -> Common.changeMiningMasterGem(json, "sortilege:blizzard"));
            register(Identifier.of("miningmaster", "recipes/smithing/fire_ruby_smithing.json"),
                    () -> ModEnchants.BRAZIER != null && ConfigEntries.miningMasterIntegration,
                    json -> Common.changeMiningMasterGem(json, "sortilege:brazier"));
            register(Identifier.of("miningmaster", "recipes/smithing/air_malachite_smithing.json"),
                    () -> ModEnchants.STABILITY != null && ConfigEntries.miningMasterIntegration,
                    json -> Common.changeMiningMasterGem(json, "sortilege:stability"));
            register(Identifier.of("miningmaster", "recipes/smithing/spirit_garnet_smithing.json"),
                    () -> ModEnchants.WISDOM != null && ConfigEntries.miningMasterIntegration,
                    json -> Common.changeMiningMasterGem(json, "sortilege:wisdom"));
            register(Identifier.of("miningmaster", "recipes/smithing/haste_peridot_smithing.json"),
                    () -> ModEnchants.FOCUS != null && ConfigEntries.miningMasterIntegration,
                    json -> Common.changeMiningMasterGem(json, "sortilege:focus"));
        }
    }

    public static void registerClient() {
        for (Pair<String, ModConfig.StaffInfo> staff : ModConfig.STAFFS)
            register(Sortilege.makeID("models/item/" + staff.getFirst() + ".json"),
                    () -> FabricLoader.getInstance().isModLoaded(staff.getSecond().dependency),
                    json -> Client.generateDefaultModel(json, staff.getFirst()));

        register(Sortilege.makeID("lang/en_us.json"), () -> true, Client::generateTranslations);

        register(Identifier.of("enchdesc", "lang/en_us.json"),
                () -> FabricLoader.getInstance().isModLoaded("enchdesc"),
                Client::changeEnchantmentDescriptions);

        register(Identifier.of("quark", "attribute_tooltips.json"),
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

            for (Pair<String, ModConfig.StaffInfo> staff : ModConfig.STAFFS) {
                if (!FabricLoader.getInstance().isModLoaded(staff.getSecond().dependency)) continue;
                json.getAsJsonObject().get("values").getAsJsonArray().add(Sortilege.makeID(staff.getFirst()).toString());
            }
            return json.toString();
        }

        public static String changeVoluntaryExileParent(JsonElement json) {
            json.getAsJsonObject().asMap().replace("parent", new JsonPrimitive(Sortilege.makeID("get_witch_hat").toString()));
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

            for (Pair<String, ModConfig.StaffInfo> staff : ModConfig.STAFFS) {
                if (!FabricLoader.getInstance().isModLoaded(staff.getSecond().dependency)) continue;

                String id = staff.getFirst();
                StringBuilder translation = new StringBuilder(id.toUpperCase().charAt(0) + "");
                for (int i = 1; i < id.length(); i++) {
                    if (id.charAt(i - 1) == '_')
                        translation.append(id.toUpperCase().charAt(i));
                    else if (id.charAt(i) == '_')
                        translation.append(' ');
                    else
                        translation.append(id.charAt(i));
                }
                json.getAsJsonObject().addProperty("item." + Sortilege.MOD_ID + "." + id, translation.toString());
            }

            if (ConfigEntries.betterMagicProt)
                json.getAsJsonObject().asMap().replace("enchantment.sortilege.magic_protection.desc",
                        new JsonPrimitive("Reduces damage from magic, and gives a chance to dodge attacks."));

            return json.toString();
        }

        public static String changeEnchantmentDescriptions(JsonElement json) {
            if (json == null) return "{}";
            JsonObject o = (JsonObject) json;

            if (ConfigEntries.betterFireProt > 0)
                o.asMap().replace("enchantment.minecraft.fire_protection.desc",
                        new JsonPrimitive(o.get("enchantment.minecraft.fire_protection.desc").getAsString()
                                + " Wearing a full set at max level completely negates them."));
            if (ConfigEntries.betterBane)
                o.asMap().replace("enchantment.minecraft.bane_of_arthropods.desc",
                        new JsonPrimitive(o.get("enchantment.minecraft.bane_of_arthropods.desc").getAsString()
                                + " Also slows down opponents."));
            if (ConfigEntries.betterFeatherFalling > 0)
                o.asMap().replace("enchantment.minecraft.feather_falling.desc",
                        new JsonPrimitive(o.get("enchantment.minecraft.feather_falling.desc").getAsString()
                                + " They are completely negated at max level."));
            if (ConfigEntries.betterUnbreaking > 0)
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
