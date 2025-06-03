package net.lyof.sortilege.setup;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.custom.potion.CustomPotionData;
import net.lyof.sortilege.item.custom.potion.PotionCooldownManager;
import net.lyof.sortilege.recipe.brewing.BetterBrewingRegistry;
import net.lyof.sortilege.recipe.brewing.custom.ItemBrewingRecipe;
import net.lyof.sortilege.recipe.crafting.RecipeLock;
import net.lyof.sortilege.recipe.emi.SpecialSmithingEmiRecipe;
import net.lyof.sortilege.recipe.enchanting.EnchantingCatalyst;
import net.lyof.sortilege.util.ItemHelper;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.Map;

public class ReloadListener implements SimpleSynchronousResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return Sortilege.makeID("reload_listener");
    }

    @Override
    public void reload(ResourceManager manager) {
        ItemHelper.ENCHLIMIT_CACHE.clear();

        // Recipe locks
        RecipeLock.clear();
        for (Map.Entry<String, Object> entry : ConfigEntries.xpRequirements.entrySet()) {
            RecipeLock.register(entry.getKey(), entry.getValue() instanceof Double d ?
                    new RecipeLock.LevelLock(d.intValue()) : new RecipeLock.AdvancementLock(String.valueOf(entry.getValue())));
        }

        // Brewing recipes
        PotionHelper.clear();

        BetterBrewingRegistry.clear();
        BetterBrewingRegistry.register();

        CustomPotionData.clear();
        PotionCooldownManager.clear();

        // Enchantment catalysts
        EnchantingCatalyst.clear();

        for (Map.Entry<Identifier, Resource> entry : manager.findResources("recipes",
                path -> path.toString().endsWith(".json")).entrySet()) {

            try {
                Resource resource = entry.getValue();

                String content = new String(resource.getInputStream().readAllBytes());
                JsonElement json = new Gson().fromJson(content, JsonElement.class);

                if (json == null || !json.isJsonObject()) continue;
                JsonObject jsono = json.getAsJsonObject();

                if (jsono.has("type") && jsono.get("type").getAsString().equals(Sortilege.MOD_ID + ":item_brewing"))
                    ItemBrewingRecipe.read(jsono);
                else if (jsono.has("type") && jsono.get("type").getAsString().equals(Sortilege.MOD_ID + ":potion_brewing"))
                    BetterBrewingRegistry.register(jsono);
                else if (jsono.has("type") && jsono.get("type").getAsString().equals(Sortilege.MOD_ID + ":enchanting_catalyst"))
                    EnchantingCatalyst.read(jsono);

            } catch (Throwable e) {
                Sortilege.log("Could not read data file " + entry.getKey());
            }
        }

        for (Map.Entry<Identifier, Resource> entry : manager.findResources("potions",
                path -> path.toString().endsWith(".json")).entrySet()) {

            try {
                Resource resource = entry.getValue();

                String content = new String(resource.getInputStream().readAllBytes());
                JsonElement json = new Gson().fromJson(content, JsonElement.class);

                if (json == null || !json.isJsonObject()) continue;

                CustomPotionData.read(json.getAsJsonObject());
            }
            catch (Throwable e) {
                Sortilege.log("Could not read data file " + entry.getKey());
            }
        }

        PotionHelper.load();

        if (FabricLoader.getInstance().isModLoaded("emi"))
            SpecialSmithingEmiRecipe.INSTANCES.forEach(SpecialSmithingEmiRecipe::generateInputs);
    }
}
