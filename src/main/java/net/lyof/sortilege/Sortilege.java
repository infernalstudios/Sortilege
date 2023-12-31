package net.lyof.sortilege;

import com.mojang.logging.LogUtils;
import net.lyof.sortilege.attributes.ModAttributes;
import net.lyof.sortilege.configs.ModJsonConfigs;
import net.lyof.sortilege.enchants.ModEnchants;
import net.lyof.sortilege.items.ModItems;
import net.lyof.sortilege.loot.ModLootModifiers;
import net.lyof.sortilege.particles.ModParticles;
import net.lyof.sortilege.recipes.AntidoteBrewingRecipe;
import net.lyof.sortilege.recipes.PotionBrewingRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Sortilege.MOD_ID)
public class Sortilege {
    public static final String MOD_ID = "sortilege";

    protected static final Logger LOGGER = LogUtils.getLogger();

    public Sortilege() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModJsonConfigs.register();

        ModItems.register(eventBus);
        ModEnchants.register(eventBus);
        ModAttributes.register(eventBus);
        ModParticles.register(eventBus);
        ModLootModifiers.register(eventBus);

        eventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> BrewingRecipeRegistry.addRecipe(new AntidoteBrewingRecipe()));
        event.enqueueWork(() -> BrewingRecipeRegistry.addRecipe(new PotionBrewingRecipe()));
    }

    public static <T> T log(T message) {
        LOGGER.debug(String.valueOf(message));
        return message;
    }
}