package net.lyof.sortilege.item.staff;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.lcc.sollib.core.Identifier;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.util.GsonHelper;

import java.util.*;
import java.util.function.Supplier;

// Yes I'm aware naming them all getThing is stupid but else it messes with my brain
public record StaffEntry(String getID, int getSortIndex, IStaffEntryReader getReader,
                         StaffTier getTier, Cost getCost, Effects getEffects, Display getDisplay) {

    public static StaffEntry read(JsonObject json, Set<ResourceLocation> recipes) throws JsonSyntaxException {
        for (JsonElement elm : GsonHelper.getAsJsonArray(json, "recipes", new JsonArray()))
            recipes.add(Identifier.of(elm.getAsString()));

        String dependency = GsonHelper.getAsString(json, "dependency", "minecraft");
        if (!FabricLoader.getInstance().isModLoaded(dependency))
            return fail();

        String type = GsonHelper.getAsString(json, "type");
        IStaffEntryReader reader = IStaffEntryReader.getFor(type);
        if (reader == null)
            return fail();

        String id = GsonHelper.getAsString(json, "id");
        int sortIndex = GsonHelper.getAsInt(json, "sort_index", Integer.MAX_VALUE);

        Sortilege.log().info("Found reader", reader, "for", id);

        StaffTier tier = StaffTier.read(GsonHelper.getAsJsonObject(json, "properties"));
        Cost cost = reader.readCost(GsonHelper.getAsJsonObject(json, "cost", new JsonObject()));
        Effects effects = reader.readEffects(GsonHelper.getAsJsonObject(json, "effects", new JsonObject()));
        Display display = reader.readDisplay(GsonHelper.getAsJsonObject(json, "display", new JsonObject()));

        return new StaffEntry(id, sortIndex, reader, tier, cost, effects, display);
    }

    private static StaffEntry fail() {
        return null;
    }

    public AStaffItem makeStaff() {
        return this.getReader().make(this);
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

        public Effects read(JsonObject json) {
            this.onShoot = GsonHelper.getAsString(json, "on_shoot", null);
            this.onHitSelf = GsonHelper.getAsString(json, "on_hit_self", null);
            this.onHitTarget = GsonHelper.getAsString(json, "on_hit_target", null);

            this.enchants = new HashMap<>();
            JsonObject enchants = GsonHelper.getAsJsonObject(json, "enchants", new JsonObject());
            for (String key : enchants.keySet())
                this.enchants.put(Identifier.of(key), GsonHelper.getAsInt(enchants, key));

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
    }

    public static class Display {
        protected ResourceLocation particle;
        protected ResourceLocation sound;
        protected List<float[]> colors;

        public Display read(JsonObject json) {
            this.particle = Identifier.of(GsonHelper.getAsString(json, "particle", "sortilege:wisp_pixel"));
            this.sound = Identifier.of(GsonHelper.getAsString(json, "particle", "minecraft:block.amethyst_block.hit"));

            this.colors = new ArrayList<>();
            JsonArray colors = GsonHelper.getAsJsonArray(json, "colors", new JsonArray());
            for (JsonElement e : colors) {
                int rgb = Integer.decode(e.getAsString());
                this.colors.add(new float[]{FastColor.ARGB32.red(rgb) / 255f,
                        FastColor.ARGB32.green(rgb) / 255f,
                        FastColor.ARGB32.blue(rgb) / 255f});
            }

            return this;
        }

        public ParticleType<?> getParticle() {
            return BuiltInRegistries.PARTICLE_TYPE.get(this.particle);
        }

        public SoundEvent getSound() {
            return BuiltInRegistries.SOUND_EVENT.get(this.sound);
        }

        public List<float[]> getColors() {
            return this.colors;
        }
    }
}
