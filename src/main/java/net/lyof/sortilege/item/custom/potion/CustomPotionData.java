package net.lyof.sortilege.item.custom.potion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.mixin.accessor.RegistryEntryReferenceAccessor;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomPotionData {
    public Identifier potion;
    public List<StatusEffectInstance> effects;
    public int drinkingTime;
    public int cooldown;
    public int stackSize;

    public CustomPotionData(Identifier potion, List<StatusEffectInstance> effects, int drinkingTime, int cooldown,
                            int stackSize, boolean create) {
        this.potion = potion;
        this.effects = effects;
        this.drinkingTime = drinkingTime;
        this.cooldown = cooldown;
        this.stackSize = stackSize;

        if (create && !Registries.POTION.containsId(potion))
            REGISTRY.putIfAbsent(potion, new Potion("custom." + potion.getNamespace() + "_" + potion.getPath()));
    }


    public static void read(JsonObject json) {
        if (json.has("potion")) {
            INSTANCES.add(new CustomPotionData(new Identifier(json.get("potion").getAsString()),
                    json.has("effects") && json.get("effects").isJsonArray() ?
                            readEffectList(json.get("effects").getAsJsonArray()) : null,
                    json.has("drinking_time") ?
                            json.get("drinking_time").getAsInt() : ConfigEntries.potionDrinkingTime,
                    json.has("cooldown") ?
                            json.get("cooldown").getAsInt() : ConfigEntries.potionCooldown,
                    json.has("stack_size") ?
                            json.get("stack_size").getAsInt() : ConfigEntries.potionStackSize,
                    json.has("create") && json.get("create").getAsBoolean()));
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


    private static final List<CustomPotionData> INSTANCES = new ArrayList<>();
    private static final Map<Potion, CustomPotionData> CACHE = new HashMap<>();

    @Nullable
    public static CustomPotionData get(Potion potion) {
        if (CACHE.containsKey(potion)) return CACHE.get(potion);
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

        REGISTRY.clear();
    }

    public static boolean isEmpty() {
        return INSTANCES.isEmpty();
    }


    private static final Map<Identifier, Potion> REGISTRY = new HashMap<>();
    public static final List<Identifier> MODELS = new ArrayList<>();

    public static Potion get(Identifier id) {
        return REGISTRY.get(id);
    }

    public static Identifier getId(Potion potion) {
        for (Map.Entry<Identifier, Potion> entry : REGISTRY.entrySet())
            if (entry.getValue() == potion) return entry.getKey();
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<RegistryEntry.Reference<T>> getRegistry(RegistryEntryOwner<T> owner) {
        return REGISTRY.entrySet().stream().map(entry -> {
            RegistryEntry.Reference<T> ref = RegistryEntry.Reference.standAlone(owner,
                    (RegistryKey<T>) RegistryKey.of(RegistryKeys.POTION, entry.getKey()));
            ((RegistryEntryReferenceAccessor<T>) ref).setValue((T) entry.getValue());
            return ref;
        }).toList();
    }
}
