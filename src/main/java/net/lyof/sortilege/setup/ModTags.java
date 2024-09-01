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
        public static TagKey<Item> SOULBINDERS = create("is_soulbind_material");
        public static TagKey<Item> LIMIT_BREAKER = create("is_limit_break_material");
        public static TagKey<Item> KEEP_ON_DEATH = create("keep_on_death");
        public static TagKey<Item> UNBREAKABLE = create("unbreakable");

        private static TagKey<Item> create(String name) {
            return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(Sortilege.MOD_ID, name));
        }
    }
}
