package net.lyof.sortilege.setup;

import net.lyof.sortilege.Sortilege;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public class ModTags {
    public static class Entities {
        public static TagKey<EntityType<?>> BOUNTIES = create("bounties");

        private static TagKey<EntityType<?>> create(String name) {
            return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(Sortilege.MOD_ID, name));
        }
    }

    public static class Items {
        public static TagKey<Item> SOULBINDERS = create("soulbinders");

        private static TagKey<Item> create(String name) {
            return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(Sortilege.MOD_ID, name));
        }
    }
}
