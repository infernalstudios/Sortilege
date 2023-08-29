package net.lyof.sortilege.items;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.ModJsonConfigs;
import net.lyof.sortilege.items.custom.StaffItem;
import net.lyof.sortilege.items.custom.WitchHatItem;
import net.lyof.sortilege.items.custom.potion.AntidotePotionItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Sortilege.MOD_ID);

    public static void register(IEventBus eventbus) {
        // STAFF DATA REGISTRY
        for (String id : ModJsonConfigs.STAFFS.keySet()) {
            if (ModList.get().isLoaded(ModJsonConfigs.STAFFS.get(id).dependency))
                STAFFS.put(id, ITEMS.register(id, () -> new StaffItem(ModJsonConfigs.STAFFS.get(id))));
        }

        ITEMS.register(eventbus);
    }

    // STAFFS
    public static Map<String, RegistryObject<Item>> STAFFS = new HashMap<>();

    // POTIONS
    public static final RegistryObject<Item> ANTIDOTE = ITEMS.register("antidote",
            () -> new AntidotePotionItem(new Item.Properties().tab(CreativeModeTab.TAB_BREWING).stacksTo(4)));

    // WITCH HAT
    public static final RegistryObject<Item> WITCH_HAT = ITEMS.register("witch_hat",
            () -> new WitchHatItem(new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
}
