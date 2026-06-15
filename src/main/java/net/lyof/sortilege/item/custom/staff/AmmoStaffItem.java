package net.lyof.sortilege.item.custom.staff;

import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;
import net.lcc.sollib.core.Identifier;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.entry.ValueCost;
import net.lyof.sortilege.util.XPHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.function.Supplier;

public class AmmoStaffItem extends AStaffItem {
    @Dependency(mod = "sortilege:ammo")
    public static class Reader implements IStaffEntryReader {
        @Override
        public StaffEntry.Cost readCost(JsonObject json) {
            return new Cost().read(json);
        }

        @Override
        public AStaffItem make(StaffEntry entry) {
            return new AmmoStaffItem(entry, new Properties());
        }
    }

    protected static class Cost extends StaffEntry.Cost {
        protected Supplier<Ingredient> items;
        protected Supplier<Component> itemsTranslation;
        protected int count;

        @Override
        public StaffEntry.Cost read(JsonObject json) {
            super.read(json);

            String items = GsonHelper.getAsString(json, "items");
            if (items.startsWith("#")) {
                ResourceLocation id = Identifier.of(items.substring(1));
                TagKey<Item> tag = TagKey.create(Registries.ITEM, id);
                this.itemsTranslation = Suppliers.memoize(() -> Component.translatable("tag.item." + id.getNamespace() + "." + id.getPath()));
                this.items = Suppliers.memoize(() -> Ingredient.of(tag));
            } else {
                ResourceLocation id = Identifier.of(items);
                this.itemsTranslation = Suppliers.memoize(() -> BuiltInRegistries.ITEM.get(id).getDescription());
                this.items = Suppliers.memoize(() -> Ingredient.of(BuiltInRegistries.ITEM.get(id)));
            }

            this.count = GsonHelper.getAsInt(json, "count", 1);
            return this;
        }

        public int getCount() {
            return this.count;
        }

        public Ingredient getItems() {
            return items.get();
        }

        public Component getItemsTranslation() {
            return ((MutableComponent) this.itemsTranslation.get()).withStyle(ChatFormatting.GRAY);
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
            if (this.cost.getItems().test(s) && s != stack)
                c -= s.getCount();
            if (c <= 0) return true;
        }
        return false;
    }

    @Override
    public void consumeResource(ItemStack stack, Player player) {
        int c = this.cost.getCount();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (this.cost.getItems().test(s) && s != stack) {
                int k = Math.min(c, s.getCount());
                s.shrink(k);
                c -= k;
            }
            if (c <= 0) return;
        }
    }

    @Override
    public void appendExtraTooltip(ItemStack stack, Player player, List<Component> tooltip) {
        if (cost.getCount() == 1)
            tooltip.add(Component.translatable("sortilege.staff.cost.item.single", cost.getItemsTranslation())
                    .withStyle(ChatFormatting.DARK_GRAY));
        else
            tooltip.add(Component.translatable("sortilege.staff.cost.item", cost.getCount(), cost.getItemsTranslation())
                    .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.empty());
    }
}
