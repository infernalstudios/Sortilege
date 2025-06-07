package net.lyof.sortilege.setup;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.impl.resource.loader.FabricLifecycledResourceManager;
import net.fabricmc.loader.api.FabricLoader;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.config.ModConfig;
import net.lyof.sortilege.item.custom.potion.CustomPotionData;
import net.lyof.sortilege.item.custom.potion.PotionCooldownManager;
import net.lyof.sortilege.recipe.brewing.BetterBrewingRegistry;
import net.lyof.sortilege.recipe.crafting.RecipeLock;
import net.lyof.sortilege.recipe.emi.SpecialSmithingEmiRecipe;
import net.lyof.sortilege.recipe.enchanting.EnchantingCatalyst;
import net.lyof.sortilege.util.ItemHelper;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.util.Map;

public class ReloadListener implements SimpleSynchronousResourceReloadListener, EarlyReloadListener {
    public static final ReloadListener INSTANCE = new ReloadListener();

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
        PotionHelper.load();

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

                if (jsono.has("type") && jsono.get("type").getAsString().equals(Sortilege.MOD_ID + ":enchanting_catalyst"))
                    EnchantingCatalyst.read(jsono);

            } catch (Throwable e) {
                Sortilege.log("Could not read data file " + entry.getKey());
            }
        }

        if (FabricLoader.getInstance().isModLoaded("emi"))
            SpecialSmithingEmiRecipe.INSTANCES.forEach(SpecialSmithingEmiRecipe::generateInputs);
    }

    @Override
    public void preload(ResourceManager manager) {
        ModConfig.register();

        BetterBrewingRegistry.clear();

        CustomPotionData.clear();
        PotionCooldownManager.clear();

        if (manager instanceof FabricLifecycledResourceManager fabricManager && fabricManager.fabric_getResourceType() == ResourceType.CLIENT_RESOURCES) {
            CustomPotionData.MODELS.clear();
            for (Identifier model : ResourceFinder.json("models/item/potions").findResources(manager).keySet())
                CustomPotionData.MODELS.add(ResourceFinder.json("models/item").toResourceId(model));
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
    }

    public void reloadClient() {
        ItemHelper.ENCHLIMIT_CACHE.clear();

        RecipeLock.clear();
        for (Map.Entry<String, Object> entry : ConfigEntries.xpRequirements.entrySet()) {
            RecipeLock.register(entry.getKey(), entry.getValue() instanceof Double d ?
                    new RecipeLock.LevelLock(d.intValue()) : new RecipeLock.AdvancementLock(String.valueOf(entry.getValue())));
        }

        PotionHelper.clear();
        PotionHelper.load();

        if (FabricLoader.getInstance().isModLoaded("emi"))
            SpecialSmithingEmiRecipe.INSTANCES.forEach(SpecialSmithingEmiRecipe::generateInputs);
    }
}
