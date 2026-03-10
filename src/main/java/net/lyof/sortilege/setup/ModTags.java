package net.lyof.sortilege.setup;

import net.lyof.sortilege.Sortilege;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModTags {
    public static class Entities {
        public static final TagKey<EntityType<?>> BOUNTIES = create("bounties");
        public static final TagKey<EntityType<?>> UNEXPERIENCED = create("unexperienced");

        private static TagKey<EntityType<?>> create(String name) {
            return TagKey.of(RegistryKeys.ENTITY_TYPE, Sortilege.makeID(name));
        }

        public static final TagKey<EntityType<?>> UNDEAD = TagKey.of(RegistryKeys.ENTITY_TYPE,
                Identifier.of("minecraft", "undead"));
        public static final TagKey<EntityType<?>> UNDERGARDEN_ENTITIES = TagKey.of(RegistryKeys.ENTITY_TYPE,
                Identifier.of("undergarden", "undergarden_entities"));
        public static final TagKey<EntityType<?>> ROTSPAWN = TagKey.of(RegistryKeys.ENTITY_TYPE,
                Identifier.of("undergarden", "rotspawn"));
    }

    public static class Items {
        public static final TagKey<Item> SOULBINDERS = create("is_soulbind_material");
        public static final TagKey<Item> LIMIT_BREAKER = create("is_limit_break_material");
        public static final TagKey<Item> SOULBIND_BLACKLIST = create("soulbind_blacklist");

        public static final TagKey<Item> KEEP_ON_DEATH = create("kept_on_death");
        public static final TagKey<Item> UNBREAKABLE = create("unbreakable");

        public static final TagKey<Item> NO_DYE_OVERLAY_STAFFS = create("staffs/no_dye_overlay");

        public static final TagKey<Item> REFILLS_CAULDRONS = create("refills_cauldrons");

        private static TagKey<Item> create(String name) {
            return TagKey.of(RegistryKeys.ITEM, Sortilege.makeID(name));
        }

        public static final TagKey<Item> XP_BOOSTED = TagKey.of(RegistryKeys.ITEM, new Identifier("phantasm", "has_xp_boost"));
        public static final TagKey<Item> KINETIC_BOOSTED = TagKey.of(RegistryKeys.ITEM, new Identifier("oreganized", "has_kinetic_damage"));

        public static final TagKey<Item> CLOGGRUM_ITEMS = TagKey.of(RegistryKeys.ITEM,
                new Identifier("undergarden", "cloggrum_items"));
        public static final TagKey<Item> FROSTSTEEL_ITEMS = TagKey.of(RegistryKeys.ITEM,
                new Identifier("undergarden", "froststeel_items"));
        public static final TagKey<Item> UTHERIUM_ITEMS = TagKey.of(RegistryKeys.ITEM,
                new Identifier("undergarden", "utherium_items"));
        public static final TagKey<Item> FORGOTTEN_ITEMS = TagKey.of(RegistryKeys.ITEM,
                new Identifier("undergarden", "forgotten_items"));
    }

    public static class Blocks {
        public static final TagKey<Block> REFILLS_CAULDRONS = create("refills_cauldrons");

        private static TagKey<Block> create(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Sortilege.makeID(name));
        }
    }
}
