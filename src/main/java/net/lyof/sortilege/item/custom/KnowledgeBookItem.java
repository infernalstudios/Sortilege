package net.lyof.sortilege.item.custom;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.lyof.sortilege.setup.ModPackets;
import net.lyof.sortilege.util.ItemHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnowledgeBookItem extends Item {
    protected final Map<Enchantment, Integer> known;

    public KnowledgeBookItem(Settings settings) {
        super(settings);
        this.known = new HashMap<>();
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> list, TooltipContext context) {
        super.appendTooltip(stack, level, list, context);

        if (Screen.hasShiftDown())
            list.add(Text.translatable("item.sortilege.knowledge_book.desc").formatted(Formatting.YELLOW));
        else
            list.add(ItemHelper.getShiftTooltip());
    }

    /*
    public boolean isLearnable(ItemStack stack, Enchantment enchant, int value) {
        if (stack.isOf(Items.ENCHANTED_BOOK) || !stack.hasNbt() || !stack.getNbt().getBoolean(ITEM_KEY)) return false;
        return this.getKnownLevel(enchant) < value;
    }

    public void learn(ItemStack stack) {
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.get(stack).entrySet())
            this.learn(entry.getKey(), entry.getValue());
    }

    public void learn(Enchantment enchant, int level) {
        if (enchant == null || level <= 0) return;

        boolean flag = true;
        int current = this.getKnownLevel(enchant);
        if (current == 0) this.known.put(enchant, level);
        else if (level > current) this.known.replace(enchant, level);
        else flag = false;

        if (flag && this.player instanceof ServerPlayerEntity serverPlayer && serverPlayer.networkHandler != null) {
            PacketByteBuf packet = PacketByteBufs.create();

            packet.writeInt(Registries.ENCHANTMENT.getRawId(enchant));
            packet.writeInt(level);

            ServerPlayNetworking.send(serverPlayer, ModPackets.LEARN_ENCHANTMENT, packet);
        }
    }

    public boolean isKnown(Enchantment enchant) {
        return this.known.containsKey(enchant);
    }

    public int getKnownLevel(Enchantment enchant) {
        return this.known.getOrDefault(enchant, 0);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("EnchantKnowledge{known=");
        for (Map.Entry<Enchantment, Integer> entry : this.known.entrySet())
            builder.append(Registries.ENCHANTMENT.getId(entry.getKey()).toString()).append(": ").append(entry.getValue()).append(", ");
        return builder.append("}").toString();
    }

    public static final String BOOK_KEY = "sorti_EnchantKnowledge";
    public static final String ITEM_KEY = "sorti_IsLearnable";

    public NbtCompound write(NbtCompound nbt) {
        for (Map.Entry<Enchantment, Integer> entry : this.known.entrySet())
            nbt.putInt(Registries.ENCHANTMENT.getId(entry.getKey()).toString(), entry.getValue());
        return nbt;
    }

    public static EnchantKnowledge read(NbtCompound nbt, PlayerEntity player) {
        EnchantKnowledge self = new EnchantKnowledge(player);
        if (!nbt.contains(BOOK_KEY, NbtElement.COMPOUND_TYPE)) return self;

        nbt = nbt.getCompound(BOOK_KEY);
        for (String enchant : nbt.getKeys())
            self.learn(Registries.ENCHANTMENT.get(new Identifier(enchant)), nbt.getInt(enchant));

        return self;
    }*/
}
