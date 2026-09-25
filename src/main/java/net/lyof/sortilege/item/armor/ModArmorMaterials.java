package net.lyof.sortilege.item.armor;

import net.lyof.sortilege.Sortilege;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;

public class ModArmorMaterials {
    public static final Holder<ArmorMaterial> WITCH = Registry.registerForHolder(BuiltInRegistries.ARMOR_MATERIAL,
            Sortilege.MOD.makeID("witch_hat"),
            new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET, 1);
            }), 24, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.WITCH_CELEBRATE),
            () -> Ingredient.of(Items.PHANTOM_MEMBRANE), List.of(new ArmorMaterial.Layer(Sortilege.MOD.makeID("witch_hat"))),
            0, 0));
}