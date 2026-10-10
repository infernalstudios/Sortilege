package net.lyof.sortilege.item.staff.entry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.lyof.sortilege.item.ModItemGroups;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.StaffTier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.WeatheringCopper;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public abstract class WeatheringStaffReader implements IStaffEntryReader {
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

    public abstract Supplier<AStaffItem> make(StaffEntry entry, WeatheringCopper.WeatherState state, boolean waxed);

    // I'm not proud of this but welp. That or Java is even more verbose than I thought and it's not really my fault
    @Override
    public void register(StaffEntry entry, BiConsumer<String, Supplier<AStaffItem>> registrar) {
        Tier tier = ((Tier) entry.getTier());
        Cost cost = ((Cost) entry.getCost());
        Effects effects = ((Effects) entry.getEffects());
        Display display = ((Display) entry.getDisplay());

        StaffEntry unaffectedEntry = new StaffEntry(entry.getID(), entry.getSortIndex(), entry.getReader(),
                tier.unaffected, cost.unaffected, effects.unaffected, display.unaffected);
        StaffEntry exposedEntry = new StaffEntry("exposed_" + entry.getID(), entry.getSortIndex() + 1, entry.getReader(),
                tier.exposed, cost.exposed, effects.exposed, display.exposed);
        StaffEntry weatheredEntry = new StaffEntry("weathered_" + entry.getID(), entry.getSortIndex() + 2, entry.getReader(),
                tier.weathered, cost.weathered, effects.weathered, display.weathered);
        StaffEntry oxidizedEntry = new StaffEntry("oxidized_" + entry.getID(), entry.getSortIndex() + 3, entry.getReader(),
                tier.oxidized, cost.oxidized, effects.oxidized, display.oxidized);

        Supplier<AStaffItem> unaffected = this.make(unaffectedEntry, WeatheringCopper.WeatherState.UNAFFECTED, false);
        Supplier<AStaffItem> unaffectedWaxed = this.make(unaffectedEntry, WeatheringCopper.WeatherState.UNAFFECTED, true);
        Supplier<AStaffItem> exposed = this.make(exposedEntry, WeatheringCopper.WeatherState.EXPOSED, false);
        Supplier<AStaffItem> exposedWaxed = this.make(exposedEntry, WeatheringCopper.WeatherState.EXPOSED, true);
        Supplier<AStaffItem> weathered = this.make(weatheredEntry, WeatheringCopper.WeatherState.WEATHERED, false);
        Supplier<AStaffItem> weatheredWaxed = this.make(weatheredEntry, WeatheringCopper.WeatherState.WEATHERED, true);
        Supplier<AStaffItem> oxidized = this.make(oxidizedEntry, WeatheringCopper.WeatherState.OXIDIZED, false);
        Supplier<AStaffItem> oxidizedWaxed = this.make(oxidizedEntry, WeatheringCopper.WeatherState.OXIDIZED, true);

        registrar.accept(unaffectedEntry.getID(), unaffected);
        registrar.accept("waxed_" + unaffectedEntry.getID(), unaffectedWaxed);
        registrar.accept(exposedEntry.getID(), exposed);
        registrar.accept("waxed_" + exposedEntry.getID(), exposedWaxed);
        registrar.accept(weatheredEntry.getID(), weathered);
        registrar.accept("waxed_" + weatheredEntry.getID(), weatheredWaxed);
        registrar.accept(oxidizedEntry.getID(), oxidized);
        registrar.accept("waxed_" + oxidizedEntry.getID(), oxidizedWaxed);

        ModItemGroups.STAFF_BLACKLIST.add("waxed_" + unaffectedEntry.getID());
        ModItemGroups.STAFF_BLACKLIST.add(exposedEntry.getID());
        ModItemGroups.STAFF_BLACKLIST.add("waxed_" + exposedEntry.getID());
        ModItemGroups.STAFF_BLACKLIST.add(weatheredEntry.getID());
        ModItemGroups.STAFF_BLACKLIST.add("waxed_" + weatheredEntry.getID());
        ModItemGroups.STAFF_BLACKLIST.add(oxidizedEntry.getID());
        ModItemGroups.STAFF_BLACKLIST.add("waxed_" + oxidizedEntry.getID());

        NEXT_BY_ITEM.put(unaffectedEntry.getID(), exposedEntry.getID());
        NEXT_BY_ITEM.put(exposedEntry.getID(), weatheredEntry.getID());
        NEXT_BY_ITEM.put(weatheredEntry.getID(), oxidizedEntry.getID());
        WAX_ON_BY_ITEM.put(unaffectedEntry.getID(), "waxed_" + unaffectedEntry.getID());
        WAX_ON_BY_ITEM.put(exposedEntry.getID(), "waxed_" + exposedEntry.getID());
        WAX_ON_BY_ITEM.put(weatheredEntry.getID(), "waxed_" + weatheredEntry.getID());
        WAX_ON_BY_ITEM.put(oxidizedEntry.getID(), "waxed_" + oxidizedEntry.getID());
    }

    protected static class Tier extends StaffTier {
        protected StaffTier unaffected, exposed, weathered, oxidized;

        @Override
        public StaffTier read(JsonObject json) throws JsonSyntaxException {
            unaffected = new StaffTier();
            exposed = new StaffTier();
            weathered = new StaffTier();
            oxidized = new StaffTier();

            unaffected.read(json).read(GsonHelper.getAsJsonObject(json, "unaffected", new JsonObject()));
            exposed.read(json).read(GsonHelper.getAsJsonObject(json, "exposed", new JsonObject()));
            weathered.read(json).read(GsonHelper.getAsJsonObject(json, "weathered", new JsonObject()));
            oxidized.read(json).read(GsonHelper.getAsJsonObject(json, "oxidized", new JsonObject()));
            return this;
        }
    }

    protected static class Cost extends StaffEntry.Cost {
        protected ValueCost unaffected, exposed, weathered, oxidized;

        @Override
        public Cost read(JsonObject json) throws JsonSyntaxException {
            unaffected = new ValueCost();
            exposed = new ValueCost();
            weathered = new ValueCost();
            oxidized = new ValueCost();

            unaffected.read(json).read(GsonHelper.getAsJsonObject(json, "unaffected", new JsonObject()));
            exposed.read(json).read(GsonHelper.getAsJsonObject(json, "exposed", new JsonObject()));
            weathered.read(json).read(GsonHelper.getAsJsonObject(json, "weathered", new JsonObject()));
            oxidized.read(json).read(GsonHelper.getAsJsonObject(json, "oxidized", new JsonObject()));
            return this;
        }
    }

    protected static class Effects extends StaffEntry.Effects {
        protected StaffEntry.Effects unaffected, exposed, weathered, oxidized;

        @Override
        public Effects read(JsonObject json) throws JsonSyntaxException {
            unaffected = new StaffEntry.Effects();
            exposed = new StaffEntry.Effects();
            weathered = new StaffEntry.Effects();
            oxidized = new StaffEntry.Effects();

            unaffected.read(json).read(GsonHelper.getAsJsonObject(json, "unaffected", new JsonObject()));
            exposed.read(json).read(GsonHelper.getAsJsonObject(json, "exposed", new JsonObject()));
            weathered.read(json).read(GsonHelper.getAsJsonObject(json, "weathered", new JsonObject()));
            oxidized.read(json).read(GsonHelper.getAsJsonObject(json, "oxidized", new JsonObject()));
            return this;
        }
    }

    protected static class Display extends StaffEntry.Display {
        protected StaffEntry.Display unaffected, exposed, weathered, oxidized;

        @Override
        public Display read(JsonObject json) throws JsonSyntaxException {
            unaffected = new StaffEntry.Display();
            exposed = new StaffEntry.Display();
            weathered = new StaffEntry.Display();
            oxidized = new StaffEntry.Display();

            unaffected.read(json);
            unaffected.read(GsonHelper.getAsJsonObject(json, "unaffected", new JsonObject()));
            exposed.read(json).read(GsonHelper.getAsJsonObject(json, "exposed", new JsonObject()));
            weathered.read(json).read(GsonHelper.getAsJsonObject(json, "weathered", new JsonObject()));
            oxidized.read(json).read(GsonHelper.getAsJsonObject(json, "oxidized", new JsonObject()));
            return this;
        }
    }

    public static final BiMap<String, String> NEXT_BY_ITEM = HashBiMap.create();
    public static final BiMap<String, String> PREVIOUS_BY_ITEM = NEXT_BY_ITEM.inverse();
    public static final BiMap<String, String> WAX_ON_BY_ITEM = HashBiMap.create();
    public static final BiMap<String, String> WAX_OFF_BY_ITEM = WAX_ON_BY_ITEM.inverse();
}
