package net.lyof.sortilege.setup;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.impl.resource.loader.FabricLifecycledResourceManager;
import net.fabricmc.loader.api.FabricLoader;
import net.lcc.sollib.api.common.data.reload.IReloadListener;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.custom.potion.CustomPotionData;
import net.lyof.sortilege.item.custom.potion.PotionCooldownManager;
import net.lyof.sortilege.recipe.brewing.BetterBrewingRegistry;
import net.lyof.sortilege.recipe.crafting.RecipeLock;
import net.lyof.sortilege.recipe.emi.SpecialSmithingEmiRecipe;
import net.lyof.sortilege.recipe.enchanting.catalyst.EnchantingCatalyst;
import net.lyof.sortilege.util.EnchantHelper;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Map;

public class ReloadListener implements IReloadListener {
    public static final ReloadListener INSTANCE = new ReloadListener();

    @Override
    public void reload(ResourceManager manager) {
        RecipeLock.clear();
        for (Map.Entry<String, RecipeLock> entry : ModConfig.recipeLocks.get().entrySet())
            RecipeLock.register(entry.getKey(), entry.getValue());

        EnchantHelper.load();
        PotionHelper.load();

        for (Map.Entry<ResourceLocation, Resource> entry : manager.listResources("recipes",
                path -> path.toString().endsWith(".json")).entrySet()) {

            try {
                Resource resource = entry.getValue();

                String content = new String(resource.open().readAllBytes());
                JsonElement json = new Gson().fromJson(content, JsonElement.class);

                if (json == null || !json.isJsonObject()) continue;
                JsonObject jsono = json.getAsJsonObject();

                if (jsono.has("type") && jsono.get("type").getAsString().equals(Sortilege.MOD_ID + ":enchanting_catalyst"))
                    EnchantingCatalyst.read(jsono);

            } catch (Throwable e) {
                //Sortilege.log("Could not read data file " + entry.getKey(), 2);
            }
        }

        if (FabricLoader.getInstance().isModLoaded("emi"))
            SpecialSmithingEmiRecipe.INSTANCES.forEach(SpecialSmithingEmiRecipe::generateInputs);
    }

    @Override
    public void preload(ResourceManager manager) {
        EnchantHelper.clear();
        RecipeLock.clear();
        PotionHelper.clear();
        BetterBrewingRegistry.clear();
        CustomPotionData.clear();
        PotionCooldownManager.clear();
        EnchantingCatalyst.clear();

        if (ModConfig.customPotionTextures.get() && manager instanceof FabricLifecycledResourceManager fabricManager &&
                fabricManager.fabric_getResourceType() == PackType.CLIENT_RESOURCES) {
            CustomPotionData.MODELS.clear();
            for (ResourceLocation model : FileToIdConverter.json("models/item/potions").listMatchingResources(manager).keySet())
                CustomPotionData.MODELS.add(FileToIdConverter.json("models/item").fileToId(model));
        }

        for (Map.Entry<ResourceLocation, Resource> entry : manager.listResources("potions",
                path -> path.toString().endsWith(".json")).entrySet()) {

            try {
                Resource resource = entry.getValue();

                String content = new String(resource.open().readAllBytes());
                JsonElement json = new Gson().fromJson(content, JsonElement.class);

                if (json == null || !json.isJsonObject()) continue;

                CustomPotionData.read(json.getAsJsonObject());
            }
            catch (Throwable e) {
                //Sortilege.log("Could not read data file " + entry.getKey(), 2);
            }
        }
    }

    public void reloadClient() {
        RecipeLock.clear();
        for (Map.Entry<String, RecipeLock> entry : ModConfig.recipeLocks.get().entrySet())
            RecipeLock.register(entry.getKey(), entry.getValue());

        EnchantingCatalyst.clear();
        CustomPotionData.clear();

        EnchantHelper.clear();
        EnchantHelper.load();

        PotionHelper.clear();
        PotionHelper.load();

        if (FabricLoader.getInstance().isModLoaded("emi"))
            SpecialSmithingEmiRecipe.INSTANCES.forEach(SpecialSmithingEmiRecipe::generateInputs);
    }
}
