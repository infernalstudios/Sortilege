package net.lyof.sortilege.setup;

import com.google.gson.Gson;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.crafting.EnchantingCatalyst;
import net.lyof.sortilege.crafting.brewing.BetterBrewingRegistry;
import net.lyof.sortilege.crafting.brewing.custom.BrewingRecipe;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.config.ModConfig;
import net.lyof.sortilege.crafting.RecipeLock;
import net.lyof.sortilege.util.ItemHelper;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class ReloadListener implements SimpleSynchronousResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return Sortilege.makeID("reload_listener");
    }

    @Override
    public void reload(ResourceManager manager) {
        ItemHelper.ENCHLIMIT_CACHE.clear();
        ModConfig.register();

        // Recipe locks
        RecipeLock.clear();
        for (Map.Entry<String, Object> entry : ConfigEntries.xpRequirements.entrySet()) {
            RecipeLock.register(entry.getKey(), entry.getValue() instanceof Double d ?
                    new RecipeLock.LevelLock(d.intValue()) : new RecipeLock.AdvancementLock(String.valueOf(entry.getValue())));
        }

        // Brewing recipes
        BetterBrewingRegistry.clear();
        BetterBrewingRegistry.register();

        // Enchantment catalysts
        EnchantingCatalyst.clear();

        for (Map.Entry<Identifier, Resource> entry : manager.findResources("recipes",
                path -> path.toString().endsWith(".json")).entrySet()) {

            try {
                Resource resource = entry.getValue();

                String content = new String(resource.getInputStream().readAllBytes());
                Map<String, ?> json = new Gson().fromJson(content, Map.class);

                if (json != null && json.containsKey("type") && Objects.equals(String.valueOf(json.get("type")), Sortilege.MOD_ID + ":brewing"))
                    BrewingRecipe.read(json);
                else if (json != null && json.containsKey("type") && Objects.equals(String.valueOf(json.get("type")), Sortilege.MOD_ID + ":enchanting_catalyst"))
                    EnchantingCatalyst.read(json);

            } catch (Exception e) {
                Sortilege.log("Could not read data file " + entry.getKey());
            }
        }
    }
}
