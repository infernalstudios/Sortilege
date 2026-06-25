package net.lyof.sortilege.item.custom.staff;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.teamabnormals.caverns_and_chasms.common.item.copper.WeatheringCopperItem;
import net.lcc.sollib.core.Identifier;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.StaffTier;
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
        public StaffTier readTier(JsonObject json) {
            return new Tier().read(json);
        }

        @Override
        public StaffEntry.Cost readCost(JsonObject json) {
            return new Cost().read(json);
        }

        @Override
        public StaffEntry.Effects readEffects(JsonObject json) {
            return new Effects().read(json);
        }

        @Override
        public StaffEntry.Display readDisplay(JsonObject json) {
            return new Display().read(json);
        }

        @Override
        public void register(StaffEntry entry, BiConsumer<String, AStaffItem> registrar) {
            Tier tier = ((Tier) entry.getTier());
            Cost cost = ((Cost) entry.getCost());
            Effects effects = ((Effects) entry.getEffects());
            Display display = ((Display) entry.getDisplay());

            StaffEntry unaffectedEntry = new StaffEntry(entry.getID(), entry.getSortIndex(), entry.getReader(),
                    tier.unaffected, cost.unaffected, effects.unaffected, display.unaffected);
            StaffEntry exposedEntry = new StaffEntry("exposed_" + entry.getID(), entry.getSortIndex(), entry.getReader(),
                    tier.exposed, cost.exposed, effects.exposed, display.exposed);
            StaffEntry weatheredEntry = new StaffEntry("weathered_" + entry.getID(), entry.getSortIndex(), entry.getReader(),
                    tier.weathered, cost.weathered, effects.weathered, display.weathered);
            StaffEntry oxidizedEntry = new StaffEntry("oxidized_" + entry.getID(), entry.getSortIndex(), entry.getReader(),
                    tier.oxidized, cost.oxidized, effects.oxidized, display.oxidized);

            AStaffItem unaffected = new WeatheringExperienceStaffItem(unaffectedEntry, WeatheringCopper.WeatherState.UNAFFECTED, false, new Properties());
            AStaffItem unaffectedWaxed = new WeatheringExperienceStaffItem(unaffectedEntry, WeatheringCopper.WeatherState.UNAFFECTED, true, new Properties());
            AStaffItem exposed = new WeatheringExperienceStaffItem(exposedEntry, WeatheringCopper.WeatherState.EXPOSED, false, new Properties());
            AStaffItem exposedWaxed = new WeatheringExperienceStaffItem(exposedEntry, WeatheringCopper.WeatherState.EXPOSED, true, new Properties());
            AStaffItem weathered = new WeatheringExperienceStaffItem(weatheredEntry, WeatheringCopper.WeatherState.WEATHERED, false, new Properties());
            AStaffItem weatheredWaxed = new WeatheringExperienceStaffItem(weatheredEntry, WeatheringCopper.WeatherState.WEATHERED, true, new Properties());
            AStaffItem oxidized = new WeatheringExperienceStaffItem(oxidizedEntry, WeatheringCopper.WeatherState.OXIDIZED, false, new Properties());
            AStaffItem oxidizedWaxed = new WeatheringExperienceStaffItem(oxidizedEntry, WeatheringCopper.WeatherState.OXIDIZED, true, new Properties());

            registrar.accept(unaffectedEntry.getID(), unaffected);
            registrar.accept("waxed_" + unaffectedEntry.getID(), unaffectedWaxed);
            registrar.accept(exposedEntry.getID(), exposed);
            registrar.accept("waxed_" + exposedEntry.getID(), exposedWaxed);
            registrar.accept(weatheredEntry.getID(), weathered);
            registrar.accept("waxed_" + weatheredEntry.getID(), weatheredWaxed);
            registrar.accept(oxidizedEntry.getID(), oxidized);
            registrar.accept("waxed_" + oxidizedEntry.getID(), oxidizedWaxed);

            NEXT_BY_ITEM.put(unaffected, exposed);
            NEXT_BY_ITEM.put(exposed, weathered);
            NEXT_BY_ITEM.put(weathered, oxidized);
            WAX_ON_BY_ITEM.put(unaffected, unaffectedWaxed);
            WAX_ON_BY_ITEM.put(exposed, exposedWaxed);
            WAX_ON_BY_ITEM.put(weathered, weatheredWaxed);
            WAX_ON_BY_ITEM.put(oxidized, oxidizedWaxed);
        }
    }

    public static class Tier extends StaffTier {
        protected StaffTier unaffected, exposed, weathered, oxidized;

        @Override
        public StaffTier read(JsonObject json) throws JsonSyntaxException {
            unaffected = new StaffTier();
            exposed = new StaffTier();
            weathered = new StaffTier();
            oxidized = new StaffTier();

            unaffected.read(json);
            unaffected.read(GsonHelper.getAsJsonObject(json, "unaffected", new JsonObject()));
            exposed.read(json);
            exposed.read(GsonHelper.getAsJsonObject(json, "exposed", new JsonObject()));
            weathered.read(json);
            weathered.read(GsonHelper.getAsJsonObject(json, "weathered", new JsonObject()));
            oxidized.read(json);
            oxidized.read(GsonHelper.getAsJsonObject(json, "oxidized", new JsonObject()));
            return this;
        }
    }

    public static class Cost extends StaffEntry.Cost {
        protected ValueCost unaffected, exposed, weathered, oxidized;

        @Override
        public Cost read(JsonObject json) throws JsonSyntaxException {
            unaffected = new ValueCost();
            exposed = new ValueCost();
            weathered = new ValueCost();
            oxidized = new ValueCost();

            unaffected.read(json);
            unaffected.read(GsonHelper.getAsJsonObject(json, "unaffected", new JsonObject()));
            exposed.read(json);
            exposed.read(GsonHelper.getAsJsonObject(json, "exposed", new JsonObject()));
            weathered.read(json);
            weathered.read(GsonHelper.getAsJsonObject(json, "weathered", new JsonObject()));
            oxidized.read(json);
            oxidized.read(GsonHelper.getAsJsonObject(json, "oxidized", new JsonObject()));
            return this;
        }
    }

    public static class Effects extends StaffEntry.Effects {
        protected StaffEntry.Effects unaffected, exposed, weathered, oxidized;

        @Override
        public Effects read(JsonObject json) throws JsonSyntaxException {
            unaffected = new StaffEntry.Effects();
            exposed = new StaffEntry.Effects();
            weathered = new StaffEntry.Effects();
            oxidized = new StaffEntry.Effects();

            unaffected.read(json);
            unaffected.read(GsonHelper.getAsJsonObject(json, "unaffected", new JsonObject()));
            exposed.read(json);
            exposed.read(GsonHelper.getAsJsonObject(json, "exposed", new JsonObject()));
            weathered.read(json);
            weathered.read(GsonHelper.getAsJsonObject(json, "weathered", new JsonObject()));
            oxidized.read(json);
            oxidized.read(GsonHelper.getAsJsonObject(json, "oxidized", new JsonObject()));
            return this;
        }
    }

    public static class Display extends StaffEntry.Display {
        protected StaffEntry.Display unaffected, exposed, weathered, oxidized;

        @Override
        public Display read(JsonObject json) throws JsonSyntaxException {
            unaffected = new StaffEntry.Display();
            exposed = new StaffEntry.Display();
            weathered = new StaffEntry.Display();
            oxidized = new StaffEntry.Display();

            unaffected.read(json);
            unaffected.read(GsonHelper.getAsJsonObject(json, "unaffected", new JsonObject()));
            exposed.read(json);
            exposed.read(GsonHelper.getAsJsonObject(json, "exposed", new JsonObject()));
            weathered.read(json);
            weathered.read(GsonHelper.getAsJsonObject(json, "weathered", new JsonObject()));
            oxidized.read(json);
            oxidized.read(GsonHelper.getAsJsonObject(json, "oxidized", new JsonObject()));
            return this;
        }
    }

    public static final HashBiMap<Item, Item> NEXT_BY_ITEM = HashBiMap.create();
    public static final BiMap<Item, Item> PREVIOUS_BY_ITEM = NEXT_BY_ITEM.inverse();
    public static final HashBiMap<Item, Item> WAX_ON_BY_ITEM = HashBiMap.create();
    public static final BiMap<Item, Item> WAX_OFF_BY_ITEM = WAX_ON_BY_ITEM.inverse();

    private final WeatheringCopper.WeatherState weatherState;
    private final boolean waxed;

    public WeatheringExperienceStaffItem(StaffEntry entry, WeatheringCopper.WeatherState weatherState, boolean waxed, Properties properties) {
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
