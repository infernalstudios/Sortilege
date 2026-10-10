package net.lyof.sortilege.setup;

import com.google.gson.JsonObject;
import net.lcc.sollib.api.common.data.reload.IReloadListener;
import net.lcc.sollib.platform.Services;
import net.lyof.sortilege.item.potion.CustomPotionData;
import net.lyof.sortilege.item.potion.PotionCooldownManager;
import net.lyof.sortilege.recipe.brewing.BetterBrewingRegistry;
import net.lyof.sortilege.recipe.crafting.RecipeLock;
import net.lyof.sortilege.recipe.emi.SpecialSmithingEmiRecipe;
import net.lyof.sortilege.util.EnchantHelper;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

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

        if (Services.PLATFORM.isModLoaded("emi"))
            SpecialSmithingEmiRecipe.INSTANCES.forEach(SpecialSmithingEmiRecipe::generateInputs);
    }

    @Override
    public void preload(ResourceManager manager) {
        RecipeLock.clear();
        PotionHelper.clear();
        BetterBrewingRegistry.clear();
        CustomPotionData.clear();
        PotionCooldownManager.clear();

        if (ModConfig.customPotionTextures.get()) {
            CustomPotionData.MODELS.clear();
            for (ResourceLocation model : FileToIdConverter.json("models/item/potion").listMatchingResources(manager).keySet())
                CustomPotionData.MODELS.add(FileToIdConverter.json("models/item").fileToId(model));
        }

        for (Map.Entry<ResourceLocation, Resource> entry : FileToIdConverter.json("potion").listMatchingResources(manager).entrySet()) {
            JsonObject json = IReloadListener.open(entry);
            if (json != null) CustomPotionData.read(json);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void preloadClient() {
        EnchantHelper.setRegistry(() -> Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.ENCHANTMENT));

        RecipeLock.clear();
        for (Map.Entry<String, RecipeLock> entry : ModConfig.recipeLocks.get().entrySet())
            RecipeLock.register(entry.getKey(), entry.getValue());

        CustomPotionData.clear();
        PotionHelper.clear();
    }

    @OnlyIn(Dist.CLIENT)
    public void reloadClient() {
        EnchantHelper.load();
        PotionHelper.load();

        if (Services.PLATFORM.isModLoaded("emi"))
            SpecialSmithingEmiRecipe.INSTANCES.forEach(SpecialSmithingEmiRecipe::generateInputs);
    }
}
