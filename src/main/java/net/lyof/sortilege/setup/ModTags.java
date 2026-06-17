package net.lyof.sortilege.setup;

import net.lcc.sollib.core.Identifier;
import net.lyof.sortilege.Sortilege;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Entities {
        public static final TagKey<EntityType<?>> BOUNTIES = create("bounties");
        public static final TagKey<EntityType<?>> UNEXPERIENCED = create("unexperienced");

        private static TagKey<EntityType<?>> create(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, Sortilege.MOD.makeID(name));
        }

        public static final TagKey<EntityType<?>> UNDEAD = TagKey.create(Registries.ENTITY_TYPE,
                Identifier.of("minecraft", "undead"));
        public static final TagKey<EntityType<?>> UNDERGARDEN_ENTITIES = TagKey.create(Registries.ENTITY_TYPE,
                Identifier.of("undergarden", "undergarden_entities"));
        public static final TagKey<EntityType<?>> ROTSPAWN = TagKey.create(Registries.ENTITY_TYPE,
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
            return TagKey.create(Registries.ITEM, Sortilege.MOD.makeID(name));
        }

        public static final TagKey<Item> XP_BOOSTED = TagKey.create(Registries.ITEM, new ResourceLocation("phantasm", "has_xp_boost"));
        public static final TagKey<Item> KINETIC_BOOSTED = TagKey.create(Registries.ITEM, new ResourceLocation("oreganized", "has_kinetic_damage"));

        public static final TagKey<Item> CLOGGRUM_ITEMS = TagKey.create(Registries.ITEM,
                new ResourceLocation("undergarden", "cloggrum_items"));
        public static final TagKey<Item> FROSTSTEEL_ITEMS = TagKey.create(Registries.ITEM,
                new ResourceLocation("undergarden", "froststeel_items"));
        public static final TagKey<Item> UTHERIUM_ITEMS = TagKey.create(Registries.ITEM,
                new ResourceLocation("undergarden", "utherium_items"));
        public static final TagKey<Item> FORGOTTEN_ITEMS = TagKey.create(Registries.ITEM,
                new ResourceLocation("undergarden", "forgotten_items"));

        public static final TagKey<Item> TERRA_ITEMS = TagKey.create(Registries.ITEM,
                new ResourceLocation("botania", "terra_items"));
    }

    public static class Blocks {
        public static final TagKey<Block> REFILLS_CAULDRONS = create("refills_cauldrons");

        private static TagKey<Block> create(String name) {
            return TagKey.create(Registries.BLOCK, Sortilege.MOD.makeID(name));
        }
    }
}
