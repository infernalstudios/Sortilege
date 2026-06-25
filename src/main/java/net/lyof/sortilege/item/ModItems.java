package net.lyof.sortilege.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.lcc.sollib.api.common.registry.holder.ItemHolder;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.armor.ModArmorMaterials;
import net.lyof.sortilege.item.custom.*;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.setup.ModConfig;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModItems {
    public static List<AStaffItem> STAFFS = new ArrayList<>();

    public static void register() {
        for (StaffEntry entry : ModConfig.staffs.get()) {
            entry.getReader().register(entry, (id, staff) -> {
                staff.setName(id);
                register(true, id, () -> staff);
                STAFFS.add(staff);
            });
        }
    }

    public static Item register(boolean config, String name, Supplier<Item> item) {
        return config ? Sortilege.MOD.register(ItemHolder.class, name, item).get() : Items.AIR;
    }


    public static final Item LIMITITE = register(true, "limitite",
            () -> new LimititeItem(new FabricItemSettings()));

    public static final Item ANTIDOTE = register(true, "antidote",
            () -> new AntidotePotionItem(new FabricItemSettings().maxCount(ModConfig.antidoteStackSize.get())));

    public static final Item WITCH_HAT = register(ModConfig.witchHatEnabled.get(), "witch_hat",
            () -> new ArmorItem(ModArmorMaterials.WITCH, ArmorItem.Type.HELMET, new FabricItemSettings()));

    public static final Item LAPIS_SHIELD = register(ModConfig.lapisShieldEnabled.get(), "lapis_shield",
            () -> new LapisShieldItem(new FabricItemSettings().durability(ModConfig.lapisShieldDurability.get())));

    public static final Item KNOWLEDGE_BOOK = register(ModConfig.knowledgeEnabled.get(), "knowledge_book",
            () -> new KnowledgeBookItem(new FabricItemSettings().stacksTo(1)));
}
