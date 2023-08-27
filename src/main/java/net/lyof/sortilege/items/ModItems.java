package net.lyof.sortilege.items;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.ModStaffConfigs;
import net.lyof.sortilege.items.custom.StaffItem;
import net.lyof.sortilege.items.custom.WitchHatItem;
import net.lyof.sortilege.items.custom.potion.AntidotePotionItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class ModItems {
    public static void register(IEventBus eventbus) {
        ITEMS.register(eventbus);
    }


    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Sortilege.MOD_ID);


    // STAFF REGISTRY
    /*
    public static final RegistryObject<Item> WOODEN_STAFF = ITEMS.register("wooden_staff",
            () -> new StaffItem(Tiers.WOOD, 2, 1, 4,
                    new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> STONE_STAFF = ITEMS.register("stone_staff",
            () -> new StaffItem(Tiers.STONE, 2, 1, 6,
                    new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> IRON_STAFF = ITEMS.register("iron_staff",
            () -> new StaffItem(Tiers.IRON, 2, 1, 8,
                    new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> GOLDEN_STAFF = ITEMS.register("golden_staff",
            () -> new StaffItem(Tiers.GOLD, 2, 2, 12,
                    new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> DIAMOND_STAFF = ITEMS.register("diamond_staff",
            () -> new StaffItem(Tiers.DIAMOND, 2, 2, 12,
                    new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> NETHERITE_STAFF = ITEMS.register("netherite_staff",
            () -> new StaffItem(Tiers.NETHERITE, 2, 3, 16,
                    new Item.Properties().tab(CreativeModeTab.TAB_COMBAT).fireResistant()));
                    */
    public static Map<String, RegistryObject<Item>> STAFFS = new HashMap<>();
    static {
        Map<String, ModStaffConfigs.StaffInfo> staffs = ModStaffConfigs.read();
        for (String id : staffs.keySet()) {
            STAFFS.put(id, ITEMS.register(id,
                    () -> new StaffItem(staffs.get(id))));
        }
    }

    // POTIONS
    public static final RegistryObject<Item> ANTIDOTE = ITEMS.register("antidote",
            () -> new AntidotePotionItem(new Item.Properties().tab(CreativeModeTab.TAB_BREWING).stacksTo(4)));
    /*
    public static final RegistryObject<Item> POTION_SPRAY = ITEMS.register("potion_spray",
            () -> new PotionSprayItem(new Item.Properties().tab(CreativeModeTab.TAB_BREWING)));*/


    // WITCH HAT
    public static final RegistryObject<Item> WITCH_HAT = ITEMS.register("witch_hat",
            () -> new WitchHatItem(new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
}
