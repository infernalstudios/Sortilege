package net.lyof.sortilege.item.custom.staff;

import com.teamabnormals.caverns_and_chasms.common.item.copper.WeatheringCopperItem;
import net.fabricmc.loader.api.FabricLoader;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.entry.WeatheringStaffReader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WeatheringCopper;

public class WeatheringFeathersStaffItem extends FeathersStaffItem implements WeatheringCopperItem {
    @Dependency(mod = "caverns_and_chasms:weathering_feathers")
    public static class Reader extends WeatheringStaffReader {
        @Override
        public AStaffItem make(StaffEntry entry, WeatheringCopper.WeatherState state, boolean waxed) {
            return new WeatheringFeathersStaffItem(entry, state, waxed, new Properties());
        }

        @Override
        public ResourceLocation getType() {
            if (!FabricLoader.getInstance().isModLoaded("feathers")) return null;
            return super.getType();
        }
    }

    private final WeatheringCopper.WeatherState weatherState;
    private final boolean waxed;

    public WeatheringFeathersStaffItem(StaffEntry entry, WeatheringCopper.WeatherState weatherState, boolean waxed, Properties properties) {
        super(entry, properties);
        this.weatherState = weatherState;
        this.waxed = waxed;
    }

    @Override
    public WeatheringCopper.WeatherState getAge() {
        return this.weatherState;
    }

    @Override
    public void updateOxidation(ItemStack stack, Level world) {
        if (this.waxed) return;
        WeatheringCopperItem.super.updateOxidation(stack, world);
    }
}
