package net.lyof.sortilege.item.custom.staff;

import com.google.gson.JsonObject;
import dev.onyxstudios.cca.api.v3.component.ComponentAccess;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.PlayerAethumComponent;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.entry.ValueCost;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AffinityStaffItem extends AStaffItem {
    @Dependency(mod = "affinity:aethum")
    public static class Reader implements IStaffEntryReader {
        @Override
        public StaffEntry.Cost readCost(JsonObject json) {
            return new ValueCost().read(json);
        }

        @Override
        public AStaffItem make(StaffEntry entry) {
            return new AffinityStaffItem(entry, new Properties());
        }
    }

    protected final ValueCost cost;

    public AffinityStaffItem(StaffEntry entry, Properties properties) {
        super(entry, properties);
        this.cost = (ValueCost) this.getEntry().getCost();
    }

    @Override
    public int getCost(ItemStack stack, Player player, int original) {
        return super.getCost(stack, player, original);
    }

    @Override
    public boolean hasResource(ItemStack stack, Player player) {
        return player instanceof ComponentAccess access &&
                access.getComponent(AffinityComponents.PLAYER_AETHUM).hasAethum(this.getCost(stack, player, cost.getValue()));
    }

    @Override
    public void consumeResource(ItemStack stack, Player player) {
        if (player instanceof ComponentAccess access) {
            PlayerAethumComponent aethum = access.getComponent(AffinityComponents.PLAYER_AETHUM);
            aethum.addAethum(-this.getCost(stack, player, cost.getValue()));
        }
    }

    @Override
    public void appendTooltipCosts(ItemStack stack, Player player, List<Component> tooltip) {
        super.appendTooltipCosts(stack, player, tooltip);

        if (this.getCost(stack, player, cost.getValue()) > 0)
            tooltip.add(Component.translatable("item.affinity.staff.tooltip.consumption_per_use", this.getCost(stack, player, cost.getValue())).withStyle(ChatFormatting.GREEN));
    }
}
