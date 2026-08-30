package net.lyof.sortilege.item.staff;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.lcc.sollib.core.Identifier;
import net.lyof.sortilege.Sortilege;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.*;

// Yes I'm aware naming them all getThing is stupid but else it messes with my brain
public record StaffEntry(String getID, int getSortIndex, IStaffEntryReader getReader,
                         StaffTier getTier, Cost getCost, Effects getEffects, Display getDisplay) {

    public static StaffEntry read(JsonObject json, Set<ResourceLocation> recipes) throws JsonSyntaxException {
        for (JsonElement elm : GsonHelper.getAsJsonArray(json, "recipes", new JsonArray()))
            recipes.add(Identifier.of(elm.getAsString()));

        String dependency = GsonHelper.getAsString(json, "dependency", "minecraft");
        if (!FabricLoader.getInstance().isModLoaded(dependency))
            return null;

        List<ResourceLocation> type = new ArrayList<>();
        if (json.get("type").isJsonArray()) {
            for (JsonElement e : GsonHelper.getAsJsonArray(json, "type"))
                type.add(Identifier.of(e.getAsString()));
        } else
            type.add(Identifier.of(GsonHelper.getAsString(json, "type")));

        IStaffEntryReader reader;
        Iterator<ResourceLocation> iterator = type.iterator();
        do {
            reader = IStaffEntryReader.getFor(iterator.next().toString());
        } while (reader == null && iterator.hasNext());
        if (reader == null)
            return null;

        String id = GsonHelper.getAsString(json, "id");
        int sortIndex = GsonHelper.getAsInt(json, "sort_index", Integer.MAX_VALUE);

        Sortilege.log().info("Found reader", reader.getClass().getName(), "for", id);

        StaffTier tier = reader.readTier(GsonHelper.getAsJsonObject(json, "properties"));
        Cost cost = reader.readCost(GsonHelper.getAsJsonObject(json, "cost", new JsonObject()));
        Effects effects = reader.readEffects(GsonHelper.getAsJsonObject(json, "effects", new JsonObject()));
        Display display = reader.readDisplay(GsonHelper.getAsJsonObject(json, "display", new JsonObject()));

        return new StaffEntry(id, sortIndex, reader, tier, cost, effects, display);
    }

    public Item.Properties applyProperties(Item.Properties properties) {
        if (this.getTier().isFireproof()) properties = properties.fireResistant();
        if (this.getDisplay().getRarity() != null) properties = properties.rarity(this.getDisplay().getRarity());
        return properties;
    }


    public static class Cost {
        protected OverchargeEntry overcharge;

        public Cost read(JsonObject json) {
            this.overcharge = OverchargeEntry.from(json);
            return this;
        }

        public OverchargeEntry getOvercharge() {
            return this.overcharge;
        }
    }

    public static class Effects {
        protected String onShoot;
        protected String onHitSelf;
        protected String onHitTarget;
        protected Map<ResourceLocation, Integer> enchants;

        public Effects() {
            this.enchants = new HashMap<>();
        }

        public Effects read(JsonObject json) {
            JsonObject commands = GsonHelper.getAsJsonObject(json, "commands", new JsonObject());
            if (commands.has("on_shoot"))
                this.onShoot = GsonHelper.getAsString(commands, "on_shoot");
            if (commands.has("on_hit_self"))
                this.onHitSelf = GsonHelper.getAsString(commands, "on_hit_self");
            if (commands.has("on_hit_target"))
                this.onHitTarget = GsonHelper.getAsString(commands, "on_hit_target");

            if (json.has("enchants")) {
                this.enchants = new HashMap<>();
                JsonObject enchants = GsonHelper.getAsJsonObject(json, "enchants");
                for (String key : enchants.keySet())
                    this.enchants.put(Identifier.of(key), GsonHelper.getAsInt(enchants, key));
            }

            return this;
        }

        public String onShoot() {
            return this.onShoot;
        }

        public String onHitSelf() {
            return this.onHitSelf;
        }

        public String onHitTarget() {
            return this.onHitTarget;
        }

        public Map<ResourceLocation, Integer> getEnchants() {
            return this.enchants;
        }
    }

    public static class Display {
        protected ResourceLocation particle;
        protected ResourceLocation sound;
        protected List<Integer> colors;
        protected Rarity rarity;

        public Display() {
            this.sound = Identifier.of("minecraft", "block.amethyst_block.hit");
            this.colors = new ArrayList<>();
        }

        public Display read(JsonObject json) {
            if (json.has("particle"))
                this.particle = Identifier.of(GsonHelper.getAsString(json, "particle"));
            if (json.has("sound"))
                this.sound = Identifier.of(GsonHelper.getAsString(json, "sound"));

            if (json.has("colors")) {
                this.colors = new ArrayList<>();
                JsonArray colors = GsonHelper.getAsJsonArray(json, "colors");
                for (JsonElement e : colors) {
                    this.colors.add(Integer.decode(e.getAsString()));
                }
            }

            try {
                this.rarity = Rarity.valueOf(GsonHelper.getAsString(json, "rarity"));
            } catch (Exception ignored) {
                this.rarity = null;
            }

            return this;
        }

        public ResourceLocation getParticle() {
            return this.particle;
        }

        public SoundEvent getSound() {
            return BuiltInRegistries.SOUND_EVENT.get(this.sound);
        }

        public List<Integer> getColors() {
            return this.colors;
        }

        public Rarity getRarity() {
            return this.rarity;
        }
    }
}
