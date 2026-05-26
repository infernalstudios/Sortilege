package net.lyof.sortilege.item;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.loader.api.FabricLoader;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.config.ModConfig;
import net.lyof.sortilege.item.custom.*;
import net.lyof.sortilege.item.custom.armor.ModArmorMaterials;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
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
                STAFFS.add(register(true, id, new StaffItem(staff, new FabricItemSettings())));
        }
    }

    public static Item register(boolean config, String name, Item item) {
        return config ? Registry.register(Registries.ITEM, Sortilege.makeID(name), item) : Items.AIR;
    }


    public static final Item LIMITITE = register(true, "limitite", new LimititeItem(new FabricItemSettings()));

    public static final Item ANTIDOTE = register(ConfigEntries.antidoteEnabled, "antidote",
            new AntidotePotionItem(new FabricItemSettings().maxCount(ConfigEntries.antidoteStackSize)));

    public static final Item WITCH_HAT = register(ConfigEntries.witchHatEnabled, "witch_hat",
            new ArmorItem(ModArmorMaterials.WITCH, ArmorItem.Type.HELMET, new FabricItemSettings()));

    public static final Item LAPIS_SHIELD = register(ConfigEntries.lapisShieldEnabled, "lapis_shield",
            new LapisShieldItem(new FabricItemSettings().maxDamage(ConfigEntries.lapisShieldDurability)));

    public static final Item KNOWLEDGE_BOOK = register(ConfigEntries.knowledgeEnabled, "knowledge_book",
            new KnowledgeBookItem(new FabricItemSettings().maxCount(1)));
}
