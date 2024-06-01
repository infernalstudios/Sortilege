package net.lyof.sortilege.setup;

import com.google.gson.Gson;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.brewing.BetterBrewingRegistry;
import net.lyof.sortilege.brewing.custom.BrewingRecipe;
import net.lyof.sortilege.configs.ModConfig;
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
        ModConfig.register();

        BetterBrewingRegistry.clear();
        BetterBrewingRegistry.register();

        for (Map.Entry<Identifier, Resource> entry : manager.findResources("recipes",
                path -> path.toString().endsWith(".json")).entrySet()) {

            try {
                Resource resource = entry.getValue();

                String content = new String(resource.getInputStream().readAllBytes());
                Map<String, ?> json = new Gson().fromJson(content, Map.class);

                if (!json.containsKey("type") || !Objects.equals(String.valueOf(json.get("type")), Sortilege.MOD_ID + ":brewing")) continue;

                BrewingRecipe.read(json);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
