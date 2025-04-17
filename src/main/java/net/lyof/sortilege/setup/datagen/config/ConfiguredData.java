package net.lyof.sortilege.setup.datagen.config;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.loader.api.FabricLoader;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ModConfig;
import net.lyof.sortilege.item.ModItems;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeKeys;
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
        for (Pair<String, ModConfig.StaffInfo> staff : ModConfig.STAFFS)
            register(Sortilege.makeID("models/item/" + staff.getFirst() + ".json"),
                    () -> FabricLoader.getInstance().isModLoaded(staff.getSecond().dependency),
                    json -> Instances.generateDefaultModel(json, staff.getFirst()));
    }

    private static class Instances {
        private static JsonElement getJson(String string) {
            return gson.fromJson(string, JsonElement.class);
        }

        public static String generateDefaultModel(JsonElement json, String path) {
            if ( json != null) return json.toString();

            return """
                    {
                      "parent": "item/handheld",
                      "textures": {
                        "layer0": """ + "\"sortilege:item/" + path + "\"" + """
                      }
                    }""";
        }
    }
}
