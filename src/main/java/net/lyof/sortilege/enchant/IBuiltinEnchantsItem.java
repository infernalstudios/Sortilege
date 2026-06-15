package net.lyof.sortilege.enchant;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface IBuiltinEnchantsItem {
    Map<ResourceLocation, Integer> getBuiltinEnchantments();
}
