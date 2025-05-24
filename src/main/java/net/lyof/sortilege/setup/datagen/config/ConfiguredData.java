package net.lyof.sortilege.setup.datagen.config;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.loader.api.FabricLoader;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.config.ModConfig;
import net.minecraft.client.MinecraftClient;
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
        register(Sortilege.makeID("tags/items/staffs.json"), () -> true, Instances::generateStaffTag);
        register(Identifier.of("minecraft", "advancements/adventure/voluntary_exile.json"),
                () -> ConfigEntries.witchHatEnabled, Instances::changeVoluntaryExileParent);
    }

    public static void registerClient() {
        for (Pair<String, ModConfig.StaffInfo> staff : ModConfig.STAFFS)
            register(Sortilege.makeID("models/item/" + staff.getFirst() + ".json"),
                    () -> FabricLoader.getInstance().isModLoaded(staff.getSecond().dependency),
                    json -> Instances.generateDefaultModel(json, staff.getFirst()));

        register(Sortilege.makeID("lang/en_us.json"), () -> true, Instances::generateTranslations);
    }

    private static class Instances {
        private static JsonElement getJson(String string) {
            return gson.fromJson(string, JsonElement.class);
        }

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

            json.getAsJsonObject().asMap().replace("advancement.sortilege.get_wooden_staff",
                    new JsonPrimitive("You are a Wizard, " + MinecraftClient.getInstance().getSession().getUsername()));

            return json.toString();
        }
    }
}
