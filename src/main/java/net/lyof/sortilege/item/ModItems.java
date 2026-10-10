package net.lyof.sortilege.item;

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
    public static List<Supplier<AStaffItem>> STAFFS = new ArrayList<>();

    public static void register() {/*
        for (StaffEntry entry : ModConfig.staffs.get()) {
            entry.getReader().register(entry, (id, staff) -> {
                Supplier<AStaffItem> s = () -> {
                    AStaffItem it = staff.get();
                    it.setName(id);
                    return it;
                };
                register(true, id, (Supplier<Item>) (Object) s);
                STAFFS.add(s);
            });
        }*/
    }

    public static Supplier<Item> register(boolean config, String name, Supplier<Item> item) {
        return config ? Sortilege.MOD.register(ItemHolder.class, name, item) : () -> Items.AIR;
    }


    public static final Supplier<Item> LIMITITE = register(true, "limitite",
            () -> new LimititeItem(new Item.Properties()));

    public static final Supplier<Item> ANTIDOTE = register(true, "antidote",
            () -> new AntidotePotionItem(new Item.Properties().stacksTo(ModConfig.antidoteStackSize.get())));

    public static final Supplier<Item> WITCH_HAT = register(ModConfig.witchHatEnabled.get(), "witch_hat",
            () -> new ArmorItem(ModArmorMaterials.WITCH, ArmorItem.Type.HELMET, new Item.Properties().durability(94)));

    public static final Supplier<Item> LAPIS_SHIELD = register(ModConfig.lapisShieldEnabled.get(), "lapis_shield",
            () -> new LapisShieldItem(new Item.Properties().durability(ModConfig.lapisShieldDurability.get())));

    public static final Supplier<Item> KNOWLEDGE_BOOK = register(ModConfig.knowledgeEnabled.get(), "knowledge_book",
            () -> new KnowledgeBookItem(new Item.Properties().stacksTo(1)));
}
