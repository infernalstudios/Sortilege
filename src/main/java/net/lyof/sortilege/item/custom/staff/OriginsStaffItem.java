package net.lyof.sortilege.item.custom.staff;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import net.lcc.sollib.core.Identifier;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.entry.ValueCost;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class OriginsStaffItem extends AStaffItem {
    @Dependency(mod = "origins:resource")
    public static class Reader implements IStaffEntryReader {
        @Override
        public StaffEntry.Cost readCost(JsonObject json) {
            return new Cost().read(json);
        }

        @Override
        public AStaffItem make(StaffEntry entry) {
            return new OriginsStaffItem(entry, new Properties());
        }
    }

    protected static class Cost extends ValueCost {
        protected ResourceLocation resource;

        @Override
        public Cost read(JsonObject json) {
            super.read(json);
            this.resource = Identifier.of(GsonHelper.getAsString(json, "resource"));
            return this;
        }

        public ResourceLocation getResourceIdentifier() {
            return resource;
        }
    }


    protected final Cost cost;

    public OriginsStaffItem(StaffEntry entry, Properties properties) {
        super(entry, properties);
        this.cost = (Cost) this.getEntry().getCost();
    }

    public PowerType<VariableIntPower> getPower(Player player) {
        for (PowerType<?> powerType : PowerHolderComponent.KEY.get(player).getPowerTypes(true)) {
            if (powerType.getIdentifier().equals(cost.getResourceIdentifier())
                    && powerType.get(player) instanceof VariableIntPower)
                return (PowerType<VariableIntPower>) powerType;
        }
        return null;
    }

    @Override
    public boolean hasResource(ItemStack stack, Player player) {
        PowerType<VariableIntPower> powerType = getPower(player);
        if (powerType == null) return false;

        return powerType.get(player).getValue() >= this.getCost(stack, player, cost.getValue());
    }

    @Override
    public void consumeResource(ItemStack stack, Player player) {
        PowerType<VariableIntPower> powerType = getPower(player);
        if (powerType == null) return;

        powerType.get(player).setValue(powerType.get(player).getValue() - this.getCost(stack, player, cost.getValue()));
    }

    @Override
    public void appendTooltipCosts(ItemStack stack, Player player, List<Component> tooltip) {
        super.appendTooltipCosts(stack, player, tooltip);

        if (this.getCost(stack, player, cost.getValue()) > 0) {
            PowerType<VariableIntPower> powerType = getPower(player);
            if (powerType == null) return;

            tooltip.add(Component.translatable("sortilege.staff.cost.origins_resource",
                    this.getCost(stack, player, cost.getValue()),
                    powerType.getName().withStyle(ChatFormatting.GRAY)
            ).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
