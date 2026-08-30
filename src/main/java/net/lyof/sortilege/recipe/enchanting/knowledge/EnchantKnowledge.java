package net.lyof.sortilege.recipe.enchanting.knowledge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lyof.sortilege.item.ModDataComponents;
import net.lyof.sortilege.item.custom.KnowledgeBookItem;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.*;

public class EnchantKnowledge {
    protected final Map<Holder<Enchantment>, Integer> known;
    protected List<String> authors;

    public EnchantKnowledge() {
        this(new HashMap<>(), EnchantHelper.getEnchantCount(), new ArrayList<>());
    }

    public EnchantKnowledge(Map<Holder<Enchantment>, Integer> known, int completion, List<String> authors) {
        this.known = known;
        this.authors = authors;
    }

    public Set<Map.Entry<Holder<Enchantment>, Integer>> getEntries() {
        return this.known.entrySet();
    }

    public int getCompletion() {
        int i = 0;
        for (Map.Entry<Holder<Enchantment>, Integer> entry : this.known.entrySet())
            i += entry.getValue();
        return i;
    }

    public boolean isLearnable(ItemStack stack, Holder<Enchantment> enchant, int value) {
        if (stack.is(Items.ENCHANTED_BOOK) || !stack.has(ModDataComponents.LEARNABLE)) return false;
        return this.getKnownLevel(enchant) < value;
    }

    public void learn(ItemStack stack) {
        for (Map.Entry<Holder<Enchantment>, Integer> entry : stack.getItem() instanceof KnowledgeBookItem ?
                stack.get(ModDataComponents.KNOWLEDGE).getEntries() : stack.getEnchantments().entrySet())
            this.learn(entry.getKey(), entry.getValue());
    }

    public void learn(Holder<Enchantment> enchant, int level) {
        if (enchant == null || level <= 0) return;

        int current = this.getKnownLevel(enchant);
        if (current == 0) this.known.put(enchant, level);
        else if (level > current) this.known.replace(enchant, level);
    }

    public boolean isKnown(Holder<Enchantment> enchant) {
        return this.known.containsKey(enchant);
    }

    public int getKnownLevel(Holder<Enchantment> enchant) {
        return this.known.getOrDefault(enchant, 0);
    }

    public List<String> getAuthors() {
        return this.authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public boolean isAuthor(Player player) {
        return player.isCreative() || getAuthors().contains(player.getScoreboardName()) || getAuthors().isEmpty();
    }

    public static void toNetwork(RegistryFriendlyByteBuf buf, EnchantKnowledge knowledge) {
        buf.writeInt(knowledge.known.size());
        for (Map.Entry<Holder<Enchantment>, Integer> entry : knowledge.known.entrySet()) {
            Enchantment.STREAM_CODEC.encode(buf, entry.getKey());
            ByteBufCodecs.VAR_INT.encode(buf, entry.getValue());
        }

        ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8).encode(buf, (ArrayList<String>) knowledge.authors);
    }

    public static EnchantKnowledge fromNetwork(RegistryFriendlyByteBuf buf) {
        EnchantKnowledge self = new EnchantKnowledge();

        Holder<Enchantment> enchant;
        int level;

        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            enchant = Enchantment.STREAM_CODEC.decode(buf);
            level = ByteBufCodecs.VAR_INT.decode(buf);

            self.learn(enchant, level);
        }

        self.authors = ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8).decode(buf);

        return self;
    }

    public static final Codec<EnchantKnowledge> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    Codec.unboundedMap(Enchantment.CODEC, Codec.intRange(0, 255))
                            .fieldOf("enchantments").forGetter(it -> it.known),
                    ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("completion").xmap(it -> it.orElse(-1), Optional::of).forGetter(EnchantKnowledge::getCompletion),
                    ExtraCodecs.NON_EMPTY_STRING.listOf().fieldOf("authors").forGetter(it -> it.authors)
            ).apply(instance, EnchantKnowledge::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantKnowledge> STREAM_CODEC =
            StreamCodec.of(EnchantKnowledge::toNetwork, EnchantKnowledge::fromNetwork);
}
