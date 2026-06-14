package net.lyof.sortilege.item.custom.staff;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.lcc.sollib.SolLib;
import net.lcc.sollib.core.Identifier;
import net.lcc.sollib.platform.Dependency;
import net.lcc.sollib.platform.Services;
import net.lyof.sortilege.Sortilege;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

// Yes I'm aware naming them all getThing is stupid but else it messes with my brain
public record StaffEntry(String getID, int getSortIndex, ResourceLocation getType, String getDependency,
                         StaffTier getTier, Cost getCost, Effects getEffects, Display getDisplay,
                         Set<ResourceLocation> getRecipes) {

    public static StaffEntry read(JsonObject json) throws JsonSyntaxException {
        String dependency = GsonHelper.getAsString(json, "dependency", "minecraft");
        if (!FabricLoader.getInstance().isModLoaded(dependency))
            return null;

        String type = GsonHelper.getAsString(json, "type");
        IStaffEntryReader reader = IStaffEntryReader.getFor(type);
        Sortilege.log().info(reader);
        if (reader == null)
            return null;

        String id = GsonHelper.getAsString(json, "id");
        int sortIndex = GsonHelper.getAsInt(json, "sort_index", -1);

        StaffTier tier = StaffTier.read(GsonHelper.getAsJsonObject(json, "properties"));
        Cost cost = reader.readCost(GsonHelper.getAsJsonObject(json, "cost", new JsonObject()));
        Effects effects = reader.readEffects(GsonHelper.getAsJsonObject(json, "effects", new JsonObject()));
        Display display = reader.readDisplay(GsonHelper.getAsJsonObject(json, "display", new JsonObject()));

        Set<ResourceLocation> recipes = new HashSet<>();
        for (JsonElement elm : GsonHelper.getAsJsonArray(json, "recipes", new JsonArray()))
            recipes.add(Identifier.of(elm.getAsString()));

        return new StaffEntry(id, sortIndex, Identifier.of(type), dependency, tier, cost, effects, display, recipes);
    }

    @Override
    public String toString() {
        return "StaffEntry{" +
                "getID='" + getID + '\'' +
                ", getSortIndex=" + getSortIndex +
                ", getType=" + getType +
                ", getDependency='" + getDependency + '\'' +
                ", getTier=" + getTier +
                ", getCost=" + getCost +
                ", getEffects=" + getEffects +
                ", getDisplay=" + getDisplay +
                ", getRecipes=" + getRecipes +
                '}';
    }

    public static class Cost {

    }

    public static class Effects {

    }

    public static class Display {

    }
}
