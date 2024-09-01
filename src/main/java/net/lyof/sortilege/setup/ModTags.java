package net.lyof.sortilege.setup;

import net.lyof.sortilege.Sortilege;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class ModTags {
    public static class Entities {
        public static TagKey<EntityType<?>> BOUNTIES = create("bounties");

        private static TagKey<EntityType<?>> create(String name) {
            return TagKey.of(RegistryKeys.ENTITY_TYPE, Sortilege.makeID(name));
        }
    }

    public static class Items {
        public static TagKey<Item> SOULBINDERS = create("is_soulbind_material");
        public static TagKey<Item> LIMIT_BREAKER = create("is_limit_break_material");
        public static TagKey<Item> KEEP_ON_DEATH = create("keep_on_death");
        public static TagKey<Item> UNBREAKABLE = create("unbreakable");

        private static TagKey<Item> create(String name) {
            return TagKey.of(RegistryKeys.ITEM, Sortilege.makeID(name));
        }
    }
}
