package net.lyof.sortilege.setup;

import net.lyof.sortilege.Sortilege;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModTags {
    public static class Entities {
        public static TagKey<EntityType<?>> BOUNTIES = create("bounties");
        public static TagKey<EntityType<?>> UNEXPERIENCED = create("unexperienced");

        private static TagKey<EntityType<?>> create(String name) {
            return TagKey.of(RegistryKeys.ENTITY_TYPE, Sortilege.makeID(name));
        }
    }

    public static class Items {
        public static TagKey<Item> SOULBINDERS = create("is_soulbind_material");
        public static TagKey<Item> LIMIT_BREAKER = create("is_limit_break_material");
        public static TagKey<Item> SOULBIND_BLACKLIST = create("soulbind_blacklist");

        public static TagKey<Item> KEEP_ON_DEATH = create("keep_on_death");
        public static TagKey<Item> UNBREAKABLE = create("unbreakable");
        public static TagKey<Item> NO_DYE_OVERLAY_STAFFS = create("staffs/no_dye_overlay");

        public static TagKey<Item> REFILLS_CAULDRONS = create("refills_cauldrons");

        private static TagKey<Item> create(String name) {
            return TagKey.of(RegistryKeys.ITEM, Sortilege.makeID(name));
        }

        public static TagKey<Item> XP_BOOSTED = TagKey.of(RegistryKeys.ITEM, new Identifier("phantasm", "gets_xp_speed_boost"));
    }

    public static class Blocks {
        public static TagKey<Block> REFILLS_CAULDRONS = create("refills_cauldrons");

        private static TagKey<Block> create(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Sortilege.makeID(name));
        }
    }
}
