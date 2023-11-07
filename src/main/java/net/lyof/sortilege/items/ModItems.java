package net.lyof.sortilege.items;

import com.mojang.datafixers.util.Pair;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.ModJsonConfigs;
import net.lyof.sortilege.items.custom.LimititeItem;
import net.lyof.sortilege.items.custom.StaffItem;
import net.lyof.sortilege.items.custom.WitchHatItem;
import net.lyof.sortilege.items.custom.potion.AntidotePotionItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
        for (Pair<String, ModJsonConfigs.StaffInfo> pair : ModJsonConfigs.STAFFS) {
            String id = pair.getFirst();
            ModJsonConfigs.StaffInfo staff = pair.getSecond();
            if (ModList.get().isLoaded(staff.dependency))
                STAFFS.put(id, ITEMS.register(id, () -> new StaffItem(staff)));
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

    // LIMITITE
    public static final RegistryObject<Item> LIMITITE = ITEMS.register("limitite",
            () -> new LimititeItem(new Item.Properties().tab(CreativeModeTab.TAB_MATERIALS)));
}
