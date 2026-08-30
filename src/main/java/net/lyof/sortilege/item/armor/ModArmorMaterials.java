package net.lyof.sortilege.item.armor;

import net.lyof.sortilege.Sortilege;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;

public class ModArmorMaterials {
    public static final Holder<ArmorMaterial> WITCH = Registry.registerForHolder(BuiltInRegistries.ARMOR_MATERIAL,
            Sortilege.MOD.makeID("witch_hat"),
            new ArmorMaterial(Util.make(new EnumMap<ArmorItem.Type, Integer>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 1);
                map.put(ArmorItem.Type.LEGGINGS, 2);
                map.put(ArmorItem.Type.CHESTPLATE, 3);
                map.put(ArmorItem.Type.HELMET, 1);
                map.put(ArmorItem.Type.BODY, 3);
            }), 24, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.WITCH_CELEBRATE),
            () -> Ingredient.of(Items.PHANTOM_MEMBRANE), List.of(new ArmorMaterial.Layer(Sortilege.MOD.makeID("witch_hat"))), 0, 0));
}