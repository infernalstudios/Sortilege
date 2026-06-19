package net.lyof.sortilege.item.staff;

import com.google.gson.JsonObject;
import net.lcc.sollib.core.Identifier;
import net.lyof.sortilege.setup.ModConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.HashMap;
import java.util.Map;

public class OverchargeEntry {
    private int max;
    private int color;
    private boolean ignoreDurability;
    private boolean ignoreCost;
    private boolean required;
    private Map<ResourceLocation, Integer> ingredients;

    public OverchargeEntry() {
        this.max = 20;
        this.color = 0x0000ff;
        this.ignoreDurability = true;
        this.ignoreCost = true;
        this.required = false;
        this.ingredients = new HashMap<>(Map.of(
                Identifier.of("minecraft:lapis_lazuli"), 2,
                Identifier.of("minecraft:lapis_block"), 20
        ));
    }

    public OverchargeEntry(OverchargeEntry parent) {
        this();
        this.max = parent.getMax();
        this.color = parent.getColor();
        this.ignoreDurability = parent.ignoreDurability();
        this.ignoreCost = parent.ignoreCost();
        this.ingredients = parent.ingredients;
    }

    public static OverchargeEntry from(JsonObject json) {
        return json.has("overcharge")
                ? read(GsonHelper.getAsJsonObject(json, "overcharge"), ModConfig.defaultOvercharge.get())
                : ModConfig.defaultOvercharge.get();
    }

    public static OverchargeEntry read(JsonObject json) {
        OverchargeEntry self = new OverchargeEntry();

        self.max = GsonHelper.getAsInt(json, "max", 0);
        self.color = Integer.decode(GsonHelper.getAsString(json, "bar_color", "#000000"));
        self.ignoreDurability = GsonHelper.getAsBoolean(json, "ignore_durability", true);
        self.ignoreCost = GsonHelper.getAsBoolean(json, "ignore_cost", true);
        self.required = GsonHelper.getAsBoolean(json, "required", false);

        self.ingredients.clear();
        JsonObject obj = GsonHelper.getAsJsonObject(json, "ingredients", new JsonObject());
        for (String key : obj.keySet())
            self.ingredients.put(Identifier.of(key), obj.getAsJsonPrimitive(key).getAsInt());

        return self;
    }

    protected static OverchargeEntry read(JsonObject json, OverchargeEntry parent) {
        if (json.keySet().isEmpty()) {
            OverchargeEntry self = new OverchargeEntry();

            self.max = 0;
            self.color = 0;
            self.ignoreDurability = false;
            self.ignoreCost = false;
            self.required = false;
            self.ingredients.clear();

            return self;
        }

        OverchargeEntry self = new OverchargeEntry(parent);

        if (json.has("max"))
            self.max = GsonHelper.getAsInt(json, "max");
        if (json.has("bar_color"))
            self.color = Integer.decode(GsonHelper.getAsString(json, "bar_color"));
        if (json.has("ignore_durability"))
            self.ignoreDurability = GsonHelper.getAsBoolean(json, "ignore_durability");
        if (json.has("ignore_cost"))
            self.ignoreCost = GsonHelper.getAsBoolean(json, "ignore_cost");
        if (json.has("required"))
            self.ignoreCost = GsonHelper.getAsBoolean(json, "required");

        if (json.has("ingredients")) {
            self.ingredients.clear();
            JsonObject obj = GsonHelper.getAsJsonObject(json, "ingredients");
            for (String key : obj.keySet())
                self.ingredients.put(Identifier.of(key), obj.getAsJsonPrimitive(key).getAsInt());
        }

        return self;
    }

    public int getMax() {
        return this.max;
    }

    public int getColor() {
        return this.color;
    }

    public boolean ignoreDurability() {
        return this.ignoreDurability;
    }

    public boolean ignoreCost() {
        return this.ignoreCost;
    }

    public boolean isRequired() {
        return this.required;
    }

    public Map<ResourceLocation, Integer> getIngredients() {
        return this.ingredients;
    }
}
