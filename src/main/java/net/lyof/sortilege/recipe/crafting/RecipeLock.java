package net.lyof.sortilege.recipe.crafting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.lyof.sortilege.setup.ModPackets;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class RecipeLock {
    public abstract boolean matches(ServerPlayer player);
    public abstract MutableComponent getFailMessage(ServerPlayer player);
    @Environment(EnvType.CLIENT) public abstract MutableComponent getFailMessage();

    public static RecipeLock NONE = new RecipeLock() {
        @Override
        public boolean matches(ServerPlayer player) {
            return false;
        }

        @Override
        public MutableComponent getFailMessage(ServerPlayer player) {
            return Component.empty();
        }

        @Environment(EnvType.CLIENT)
        @Override
        public MutableComponent getFailMessage() {
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
            return Component.translatable("sortilege.crafting.requires_level", this.lvl);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public MutableComponent getFailMessage() {
            return Component.translatable("sortilege.crafting.requires_level",
                    Component.literal("" + this.lvl).withStyle(ChatFormatting.YELLOW));
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

        @Environment(EnvType.CLIENT)
        @Override
        public MutableComponent getFailMessage() {
            if (!TITLES.containsKey(this.id)) return Component.empty();
            return TITLES.get(this.id);
        }

        @Override
        public String toString() {
            return "AdvancementLock{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }


    private static final Map<String, MutableComponent> TITLES = new HashMap<>();

    public static void read(FriendlyByteBuf packet) {
        String id = packet.readUtf();
        boolean isTranslatable = packet.readBoolean();
        String title = packet.readUtf();

        TITLES.putIfAbsent(id, Component.translatable("sortilege.crafting.requires_advancement",
                Component.literal("[")
                        .append(isTranslatable ? Component.translatable(title) : Component.literal(title))
                        .append("]").withStyle(ChatFormatting.GREEN)));
    }

    public static void write(List<FriendlyByteBuf> packets, ServerPlayer player) {
        for (RecipeLock lock : RECIPE_LOCKS.values()) {
            if (!(lock instanceof AdvancementLock advancementLock)) continue;
            Advancement advc = Objects.requireNonNull(player.getServer()).getAdvancements()
                    .getAdvancement(new ResourceLocation(advancementLock.id));
            if (advc == null || advc.getDisplay() == null) continue;

            FriendlyByteBuf packet = PacketByteBufs.create();
            packet.writeInt(ModPackets.INIT_LOCK);

            packet.writeUtf(advancementLock.id);
            Component title = advc.getDisplay().getTitle();
            packet.writeBoolean(title.getContents() instanceof TranslatableContents);
            packet.writeUtf(title.getContents() instanceof TranslatableContents content ?
                    content.getKey() : title.getString());

            packets.add(packet);
        }
    }
}
