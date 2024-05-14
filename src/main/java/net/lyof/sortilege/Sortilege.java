package net.lyof.sortilege;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.lyof.sortilege.configs.ModJsonConfigs;
import net.lyof.sortilege.enchants.ModEnchants;
import net.lyof.sortilege.item.ModItemGroups;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.particles.ModParticles;
import net.lyof.sortilege.setup.ReloadListener;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sortilege implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Sortilege");
	public static final String MOD_ID = "sortilege";

	@Override
	public void onInitialize() {
		ModJsonConfigs.register();

		ModItems.register();
		ModItemGroups.register();

		ModEnchants.register();
		ModParticles.register();

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ReloadListener());
	}

	public static Identifier makeID(String name) {
		return Identifier.of(MOD_ID, name);
	}

	public static <T> T log(T message) {
		LOGGER.info(String.valueOf(message));
		return message;
	}
}