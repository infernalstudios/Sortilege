package net.lyof.sortilege.recipe.enchanting.knowledge;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.setup.ModPackets;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.function.EnchantRandomlyLootFunction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantKnowledge {
    protected final Map<Enchantment, Integer> known;
    protected PlayerEntity player;

    public EnchantKnowledge(PlayerEntity player) {
        this.known = new HashMap<>();
        this.player = player;
    }

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

    public static final String PLAYER_KEY = "sorti_EnchantKnowledge";
    public static final String ITEM_KEY = "sorti_IsLearnable";

    public NbtCompound write(NbtCompound nbt) {
        for (Map.Entry<Enchantment, Integer> entry : this.known.entrySet())
            nbt.putInt(Registries.ENCHANTMENT.getId(entry.getKey()).toString(), entry.getValue());
        return nbt;
    }

    public static EnchantKnowledge read(NbtCompound nbt, PlayerEntity player) {
        EnchantKnowledge self = new EnchantKnowledge(player);
        if (!nbt.contains(PLAYER_KEY, NbtElement.COMPOUND_TYPE)) return self;

        nbt = nbt.getCompound(PLAYER_KEY);
        for (String enchant : nbt.getKeys())
            self.learn(Registries.ENCHANTMENT.get(new Identifier(enchant)), nbt.getInt(enchant));

        return self;
    }

    public void write(List<PacketByteBuf> packets) {
        PacketByteBuf packet = PacketByteBufs.create();
        packet.writeInt(ModPackets.INIT_KNOWLEDGE);

        packet.writeInt(this.known.size());
        for (Map.Entry<Enchantment, Integer> entry : this.known.entrySet()) {
            packet.writeString(Registries.ENCHANTMENT.getId(entry.getKey()).toString());
            packet.writeInt(entry.getValue());
        }

        packets.add(packet);
    }

    public static EnchantKnowledge read(PacketByteBuf packet, PlayerEntity player) {
        EnchantKnowledge self = new EnchantKnowledge(player);

        int size = packet.readInt();
        for (int i = 0; i < size; i++) {
            String enchant = packet.readString();
            int level = packet.readInt();
            self.learn(Registries.ENCHANTMENT.get(new Identifier(enchant)), level);
        }

        return self;
    }
}
