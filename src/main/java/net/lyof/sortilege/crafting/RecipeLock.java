package net.lyof.sortilege.crafting;

import net.minecraft.advancement.Advancement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class RecipeLock {
    public abstract boolean matches(ServerPlayerEntity player);
    public abstract MutableText getFailMessage(ServerPlayerEntity player);

    public static RecipeLock NONE = new RecipeLock() {
        @Override
        public boolean matches(ServerPlayerEntity player) {
            return false;
        }

        @Override
        public MutableText getFailMessage(ServerPlayerEntity player) {
            return Text.empty();
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
        public boolean matches(ServerPlayerEntity player) {
            return player.experienceLevel < this.lvl;
        }

        @Override
        public MutableText getFailMessage(ServerPlayerEntity player) {
            return Text.translatable("sortilege.crafting.requires_level", lvl);
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
        public boolean matches(ServerPlayerEntity player) {
            Advancement advc = Objects.requireNonNull(player.getServer()).getAdvancementLoader().get(new Identifier(this.id));
            return advc != null && !player.getAdvancementTracker().getProgress(advc).isDone();
        }

        @Override
        public MutableText getFailMessage(ServerPlayerEntity player) {
            Advancement advc = Objects.requireNonNull(player.getServer()).getAdvancementLoader().get(new Identifier(this.id));
            if (advc == null) return Text.empty();
            return Text.translatable("sortilege.crafting.requires_advancement", advc.toHoverableText());
        }

        @Override
        public String toString() {
            return "AdvancementLock{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }
}
