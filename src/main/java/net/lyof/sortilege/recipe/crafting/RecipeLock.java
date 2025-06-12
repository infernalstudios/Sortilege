package net.lyof.sortilege.recipe.crafting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.advancement.Advancement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class RecipeLock {
    public abstract boolean matches(ServerPlayerEntity player);
    public abstract MutableText getFailMessage(ServerPlayerEntity player);
    @Environment(EnvType.CLIENT) public abstract MutableText getFailMessage();

    public static RecipeLock NONE = new RecipeLock() {
        @Override
        public boolean matches(ServerPlayerEntity player) {
            return false;
        }

        @Override
        public MutableText getFailMessage(ServerPlayerEntity player) {
            return Text.empty();
        }

        @Environment(EnvType.CLIENT)
        @Override
        public MutableText getFailMessage() {
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
            return Text.translatable("sortilege.crafting.requires_level", this.lvl);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public MutableText getFailMessage() {
            return Text.translatable("sortilege.crafting.requires_level",
                    Text.literal("" + this.lvl).formatted(Formatting.YELLOW));
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

        @Environment(EnvType.CLIENT)
        @Override
        public MutableText getFailMessage() {
            if (!TITLES.containsKey(this.id)) return Text.empty();
            return TITLES.get(this.id);
        }

        @Override
        public String toString() {
            return "AdvancementLock{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }


    private static final Map<String, MutableText> TITLES = new HashMap<>();

    public static void read(PacketByteBuf packet) {
        String id = packet.readString();
        boolean isTranslatable = packet.readBoolean();
        String title = packet.readString();

        TITLES.putIfAbsent(id, Text.translatable("sortilege.crafting.requires_advancement",
                Text.literal("[")
                        .append(isTranslatable ? Text.translatable(title) : Text.literal(title))
                        .append("]").formatted(Formatting.GREEN)));
    }

    public static void write(List<PacketByteBuf> packets, ServerPlayerEntity player) {
        for (RecipeLock lock : RECIPE_LOCKS.values()) {
            if (!(lock instanceof AdvancementLock advancementLock)) continue;
            Advancement advc = Objects.requireNonNull(player.getServer()).getAdvancementLoader()
                    .get(new Identifier(advancementLock.id));
            if (advc == null || advc.getDisplay() == null) continue;

            PacketByteBuf packet = PacketByteBufs.create();
            packet.writeInt(3);

            packet.writeString(advancementLock.id);
            Text title = advc.getDisplay().getTitle();
            packet.writeBoolean(title.getContent() instanceof TranslatableTextContent);
            packet.writeString(title.getContent() instanceof TranslatableTextContent content ?
                    content.getKey() : title.getString());

            packets.add(packet);
        }
    }
}
