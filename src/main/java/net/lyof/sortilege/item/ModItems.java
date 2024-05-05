package net.lyof.sortilege.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.custom.LimititeItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModItems {
    public static void register() {}

    public static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Sortilege.makeID(name), item);
    }

    public static final Item LIMITITE = register("limitite", new LimititeItem(new FabricItemSettings()));
}
