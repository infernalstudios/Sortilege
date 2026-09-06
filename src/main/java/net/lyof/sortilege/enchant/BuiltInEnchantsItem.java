package net.lyof.sortilege.enchant;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface BuiltInEnchantsItem {
    Map<ResourceLocation, Integer> getBuiltinEnchantments();
}
