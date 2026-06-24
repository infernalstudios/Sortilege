package net.lyof.sortilege.item.custom.staff;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import com.teamabnormals.caverns_and_chasms.common.item.copper.WeatheringCopperItem;
import net.lcc.sollib.core.Identifier;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.entry.ValueCost;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WeatheringCopper;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class WeatheringExperienceStaffItem extends ExperienceStaffItem implements WeatheringCopperItem {
    @Dependency(mod = "caverns_and_chasms:weathering_experience")
    public static class Reader implements IStaffEntryReader {
        @Override
        public StaffEntry.Cost readCost(JsonObject json) {
            return new ValueCost().read(json);
        }

        @Override
        public StaffEntry.Effects readEffects(JsonObject json) {
            return new Effects().read(json);
        }

        @Override
        public void register(StaffEntry entry, BiConsumer<String, AStaffItem> registrar) {
            AStaffItem waxoff = new WeatheringExperienceStaffItem(entry, false, new Properties());
            AStaffItem waxon = new WeatheringExperienceStaffItem(entry, true, new Properties());

            registrar.accept(entry.getID(), waxoff);
            registrar.accept("waxed_" + entry.getID(), waxon);

            WAX_ON_BY_ITEM.put(waxoff, waxon);
        }

        @Override
        public void finalize(AStaffItem self) {
            ResourceLocation id = ((Effects) self.getEntry().getEffects()).getNext();
            if (id == null) return;
            Item next = BuiltInRegistries.ITEM.get(id);
            if (next == Items.AIR) return;
            NEXT_BY_ITEM.put(self, next);
        }
    }

    public static class Effects extends StaffEntry.Effects {
        protected ResourceLocation next;
        protected WeatheringCopper.WeatherState weatherState;

        @Override
        public Effects read(JsonObject json) {
            super.read(json);
            String id = GsonHelper.getAsString(json, "next", null);
            if (id == null) this.next = null;
            else if (id.contains(":")) this.next = Identifier.of(id);
            else this.next = Sortilege.MOD.makeID(id);
            this.weatherState = WeatheringCopper.WeatherState.valueOf(GsonHelper.getAsString(json, "weather_state"));
            return this;
        }

        public ResourceLocation getNext() {
            return this.next;
        }

        public WeatheringCopper.WeatherState getWeatherState() {
            return this.weatherState;
        }
    }

    public static final HashBiMap<Item, Item> NEXT_BY_ITEM = HashBiMap.create();
    public static final BiMap<Item, Item> PREVIOUS_BY_ITEM = NEXT_BY_ITEM.inverse();
    public static final HashBiMap<Item, Item> WAX_ON_BY_ITEM = HashBiMap.create();
    public static final BiMap<Item, Item> WAX_OFF_BY_ITEM = WAX_ON_BY_ITEM.inverse();

    private final Effects effects;
    private final boolean waxed;

    public WeatheringExperienceStaffItem(StaffEntry entry, boolean waxed, Properties properties) {
        super(entry, properties);
        this.effects = (Effects) entry.getEffects();
        this.waxed = waxed;
    }

    @Override
    public WeatheringCopper.WeatherState getAge() {
        return effects.getWeatherState();
    }

    @Override
    public void updateOxidation(ItemStack stack, Level world) {
        if (this.waxed) return;
        WeatheringCopperItem.super.updateOxidation(stack, world);
    }
}
