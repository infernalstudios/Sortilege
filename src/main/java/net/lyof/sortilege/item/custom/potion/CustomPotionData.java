package net.lyof.sortilege.item.custom.potion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CustomPotionData {
    public Identifier potion;
    public List<StatusEffectInstance> effects;
    public int drinkingTime;

    public CustomPotionData(Identifier potion, List<StatusEffectInstance> effects, int drinkingTime) {
        this.potion = potion;
        this.effects = effects;
        this.drinkingTime = drinkingTime;
    }

    @Override
    public String toString() {
        return "CustomPotionData{" +
                "potion=" + potion +
                ", effects=" + effects.stream().map(e -> e.getEffectType().getTranslationKey() + " " + e.getDuration() + " " + e.getAmplifier()).toList() +
                ", drinkingTime=" + drinkingTime +
                '}';
    }

    private static final List<CustomPotionData> INSTANCES = new ArrayList<>();
    private static final Map<Potion, CustomPotionData> CACHE = new HashMap<>();

    @Nullable
    public static CustomPotionData get(Potion potion) {
        if (CACHE.containsKey(potion)) return CACHE.get(potion);
        Sortilege.log("Running lengthy calculation");
        CustomPotionData result = INSTANCES.stream().filter(data -> data.potion.equals(Registries.POTION.getId(potion)))
                .findFirst().orElse(null);
        CACHE.put(potion, result);
        return result;
    }

    public static void clear() {
        INSTANCES.clear();
        CACHE.clear();

        for (Potion potion : Registries.POTION)
            ((IPotionShenanigans) potion).sorti$resetPotionCache();
    }

    public static boolean isEmpty() {
        return INSTANCES.isEmpty();
    }

    public static void read(JsonObject json) {
        if (json.has("potion")) {

            INSTANCES.add(new CustomPotionData(new Identifier(json.get("potion").getAsString()),
                    json.has("effects") && json.get("effects").isJsonArray() ?
                            readEffectList(json.get("effects").getAsJsonArray()) : null,
                    json.has("drinking_time") ?
                            json.get("drinking_time").getAsInt() : ConfigEntries.potionDrinkingTime));
        }
    }

    public static List<StatusEffectInstance> readEffectList(JsonArray json) {
        List<StatusEffectInstance> effects = new ArrayList<>();
        for (JsonElement e : json) {
            if (!e.isJsonObject()) continue;
            JsonObject o = e.getAsJsonObject();
            if (!o.has("effect") || !o.has("duration") || !o.has("amplifier")) continue;

            StatusEffect effect = Registries.STATUS_EFFECT.get(new Identifier(o.get("effect").getAsString()));
            if (effect == null) continue;
            int duration = o.get("duration").getAsInt();
            int amplifier = o.get("amplifier").getAsInt();

            effects.add(new StatusEffectInstance(effect, duration, amplifier));
        }
        return effects;
    }
}
