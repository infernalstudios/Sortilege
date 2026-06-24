package net.lyof.sortilege.item.custom.staff;

import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;
import net.lcc.sollib.core.Identifier;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.util.EnchantHelper;
import net.lyof.sortilege.util.MathHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class AmmoStaffItem extends AStaffItem {
    @Dependency(mod = "sortilege:ammo")
    public static class Reader implements IStaffEntryReader {
        @Override
        public StaffEntry.Cost readCost(JsonObject json) {
            return new Cost().read(json);
        }

        @Override
        public void register(StaffEntry entry, BiConsumer<String, AStaffItem> registrar) {
            registrar.accept(entry.getID(), new AmmoStaffItem(entry, new Properties()));
        }
    }

    protected static class Cost extends StaffEntry.Cost {
        protected Supplier<Ingredient> item;
        protected Supplier<Component> itemTranslation;
        protected int count;

        @Override
        public StaffEntry.Cost read(JsonObject json) {
            super.read(json);

            String items = GsonHelper.getAsString(json, "item");
            if (items.startsWith("#")) {
                ResourceLocation id = Identifier.of(items.substring(1));
                TagKey<Item> tag = TagKey.create(Registries.ITEM, id);
                this.itemTranslation = Suppliers.memoize(() -> Component.translatable("tag.item." + id.getNamespace() + "." + id.getPath()));
                this.item = Suppliers.memoize(() -> Ingredient.of(tag));
            } else {
                ResourceLocation id = Identifier.of(items);
                this.itemTranslation = Suppliers.memoize(() -> BuiltInRegistries.ITEM.get(id).getDescription());
                this.item = Suppliers.memoize(() -> Ingredient.of(BuiltInRegistries.ITEM.get(id)));
            }

            this.count = GsonHelper.getAsInt(json, "count", 1);
            return this;
        }

        public int getCount() {
            return this.count;
        }

        public Ingredient getItem() {
            return item.get();
        }

        public Component getItemTranslation() {
            return ((MutableComponent) this.itemTranslation.get()).withStyle(ChatFormatting.GRAY);
        }
    }

    protected final Cost cost;

    public AmmoStaffItem(StaffEntry entry, Properties properties) {
        super(entry, properties);
        this.cost = (Cost) this.getEntry().getCost();
    }

    @Override
    public boolean hasResource(ItemStack stack, Player player) {
        int c = this.cost.getCount();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (this.cost.getItem().test(s) && s != stack)
                c -= s.getCount();
            if (c <= 0) return true;
        }
        return false;
    }

    @Override
    public void consumeResource(ItemStack stack, Player player) {
        float wisdom = EnchantHelper.getEnchantLevel(ModEnchants.WISDOM, stack) * 0.25f;
        RandomSource random = MathHelper.getRandom(player.level());
        if (random.nextFloat() < wisdom) return;

        int c = this.cost.getCount();
        if (EnchantHelper.hasEnchant(ModEnchants.IGNORANCE_CURSE, stack) && random.nextFloat() < 0.25)
            c *= 2;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (this.cost.getItem().test(s) && s != stack) {
                int k = Math.min(c, s.getCount());
                s.shrink(k);
                c -= k;
            }
            if (c <= 0) return;
        }
    }

    @Override
    public void appendTooltipCosts(ItemStack stack, Player player, List<Component> tooltip) {
        super.appendTooltipCosts(stack, player, tooltip);

        if (cost.getCount() == 1)
            tooltip.add(Component.translatable("tooltip.sortilege.staff.cost.item.single", cost.getItemTranslation()).withStyle(ChatFormatting.DARK_GRAY));
        else
            tooltip.add(Component.translatable("tooltip.sortilege.staff.cost.item", cost.getCount(), cost.getItemTranslation()).withStyle(ChatFormatting.DARK_GRAY));
    }
}
