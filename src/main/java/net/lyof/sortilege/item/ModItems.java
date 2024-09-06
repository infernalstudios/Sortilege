package net.lyof.sortilege.item;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.loader.api.FabricLoader;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.config.ModConfig;
import net.lyof.sortilege.item.custom.LimititeItem;
import net.lyof.sortilege.item.custom.StaffItem;
import net.lyof.sortilege.item.custom.armor.WitchHatItem;
import net.lyof.sortilege.item.custom.potion.AntidotePotionItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class ModItems {
    public static List<Item> STAFFS = new ArrayList<>();

    public static void register() {
        for (Pair<String, ModConfig.StaffInfo> pair : ModConfig.STAFFS) {
            String id = pair.getFirst();
            ModConfig.StaffInfo staff = pair.getSecond();
            if (FabricLoader.getInstance().isModLoaded(staff.dependency))
                STAFFS.add(register(id, new StaffItem(staff, new FabricItemSettings().maxCount(1))));
        }
    }

    public static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Sortilege.makeID(name), item);
    }


    public static final Item LIMITITE = register("limitite", new LimititeItem(new FabricItemSettings()));

    public static final Item ANTIDOTE = register("antidote",
            new AntidotePotionItem(new FabricItemSettings().maxCount(ConfigEntries.antidoteStackSize)));

    public static final Item WITCH_HAT = register("witch_hat",
            new WitchHatItem(new FabricItemSettings()));
}
