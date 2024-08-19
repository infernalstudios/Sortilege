package net.lyof.sortilege.crafting;

import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class RecipeLock {
    public abstract boolean matches(ServerPlayer player);
    public abstract MutableComponent getFailMessage(ServerPlayer player);

    public static RecipeLock NONE = new RecipeLock() {
        @Override
        public boolean matches(ServerPlayer player) {
            return false;
        }

        @Override
        public MutableComponent getFailMessage(ServerPlayer player) {
            return Component.empty();
        }
    };


    protected static Map<String, RecipeLock> RECIPE_LOCKS = new HashMap<>();

    public static void clear() {
        RECIPE_LOCKS.clear();
    }

    public static void register(String recipe, RecipeLock lock) {
        RECIPE_LOCKS.putIfAbsent(recipe, lock);
    }

    public static RecipeLock get(String recipe) {
        return RECIPE_LOCKS.getOrDefault(recipe, RecipeLock.NONE);
    }

    public static Map<String, RecipeLock> getAll() {
        return RECIPE_LOCKS;
    }


    public static class LevelLock extends RecipeLock {
        public int lvl;
        public LevelLock(int lvl) { this.lvl = lvl; }

        @Override
        public boolean matches(ServerPlayer player) {
            return player.experienceLevel < this.lvl;
        }

        @Override
        public MutableComponent getFailMessage(ServerPlayer player) {
            return Component.translatable("sortilege.crafting.requires_level", lvl);
        }

        @Override
        public String toString() {
            return "LevelLock{" +
                    "lvl=" + lvl +
                    '}';
        }
    }

    public static class AdvancementLock extends RecipeLock {
        public String id;
        public AdvancementLock(String id) { this.id = id; }

        @Override
        public boolean matches(ServerPlayer player) {
            Advancement advc = Objects.requireNonNull(player.getServer()).getAdvancements().getAdvancement(new ResourceLocation(this.id));
            return advc != null && !player.getAdvancements().getOrStartProgress(advc).isDone();
        }

        @Override
        public MutableComponent getFailMessage(ServerPlayer player) {
            Advancement advc = Objects.requireNonNull(player.getServer()).getAdvancements().getAdvancement(new ResourceLocation(this.id));
            if (advc == null) return Component.empty();
            return Component.translatable("sortilege.crafting.requires_advancement", advc.getChatComponent());
        }

        @Override
        public String toString() {
            return "AdvancementLock{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }
}
