package net.lyof.sortilege.item.custom.potion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.mixin.accessor.HolderReferenceAccessor;
import net.lyof.sortilege.setup.ModPackets;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomPotionData {
    public ResourceLocation potion;
    public List<MobEffectInstance> effects;
    public int drinkingTime;
    public int cooldown;
    public int stackSize;
    public boolean create;

    public CustomPotionData(ResourceLocation potion, List<MobEffectInstance> effects, int drinkingTime, int cooldown,
                            int stackSize, boolean create) {
        this.potion = potion;
        this.effects = effects;
        this.drinkingTime = drinkingTime;
        this.cooldown = cooldown;
        this.stackSize = stackSize;
        this.create = create;

        if (this.create && !BuiltInRegistries.POTION.containsKey(potion))
            REGISTRY.putIfAbsent(potion, new Potion("custom." + potion.getNamespace() + "." + potion.getPath()));
    }


    public static void read(JsonObject json) {
        if (json.has("potion")) {
            INSTANCES.add(new CustomPotionData(new ResourceLocation(json.get("potion").getAsString()),
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

    public static List<MobEffectInstance> readEffectList(JsonArray json) {
        List<MobEffectInstance> effects = new ArrayList<>();
        for (JsonElement e : json) {
            if (!e.isJsonObject()) continue;
            JsonObject o = e.getAsJsonObject();
            if (!o.has("effect") || !o.has("duration") || !o.has("amplifier")) continue;

            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(o.get("effect").getAsString()));
            if (effect == null) continue;
            int duration = o.get("duration").getAsInt();
            int amplifier = o.get("amplifier").getAsInt();

            effects.add(new MobEffectInstance(effect, duration, amplifier));
        }
        return effects;
    }

    public static void read(FriendlyByteBuf packet) {
        ResourceLocation potion = packet.readResourceLocation();
        int stackSize = packet.readInt();
        int drinkingTime = packet.readInt();
        int cooldown = packet.readInt();
        boolean create = packet.readBoolean();

        int size = packet.readInt();
        List<MobEffectInstance> effects;
        if (size == -1)
            effects = null;
        else {
            effects = new ArrayList<>();
            for (int j = 0; j < size; j++) {
                MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(packet.readResourceLocation());
                int duration = packet.readInt();
                int amplifier = packet.readInt();

                effects.add(new MobEffectInstance(effect, duration, amplifier));
            }
        }

        INSTANCES.add(new CustomPotionData(potion, effects, drinkingTime, cooldown, stackSize, create));
    }

    public static void write(List<FriendlyByteBuf> packets) {
        for (CustomPotionData data : INSTANCES) {
            FriendlyByteBuf packet = PacketByteBufs.create();
            packet.writeInt(ModPackets.INIT_POTION);

            packet.writeResourceLocation(data.potion);
            packet.writeInt(data.stackSize);
            packet.writeInt(data.drinkingTime);
            packet.writeInt(data.cooldown);
            packet.writeBoolean(data.create);

            if (data.effects == null)
                packet.writeInt(-1);
            else {
                packet.writeInt(data.effects.size());
                for (MobEffectInstance effect : data.effects) {
                    packet.writeResourceLocation(BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect()));
                    packet.writeInt(effect.getDuration());
                    packet.writeInt(effect.getAmplifier());
                }
            }

            packets.add(packet);
        }
    }


    private static final List<CustomPotionData> INSTANCES = new ArrayList<>();
    private static final Map<Potion, CustomPotionData> CACHE = new HashMap<>();

    @Nullable
    public static CustomPotionData get(Potion potion) {
        if (!ConfigEntries.potionData) return null;

        if (CACHE.containsKey(potion)) return CACHE.get(potion);
        CustomPotionData result = INSTANCES.stream().filter(data -> data.potion.equals(BuiltInRegistries.POTION.getKey(potion)))
                .findFirst().orElse(null);
        CACHE.put(potion, result);
        return result;
    }

    public static void clear() {
        INSTANCES.clear();
        CACHE.clear();

        for (Potion potion : BuiltInRegistries.POTION)
            ((PotionShenanigans) potion).sorti_resetPotionCache();

        REGISTRY.clear();
    }

    public static boolean isEmpty() {
        return INSTANCES.isEmpty();
    }


    private static final Map<ResourceLocation, Potion> REGISTRY = new HashMap<>();
    public static final List<ResourceLocation> MODELS = new ArrayList<>();

    public static Potion get(ResourceLocation id) {
        return REGISTRY.get(id);
    }

    public static ResourceLocation getId(Potion potion) {
        for (Map.Entry<ResourceLocation, Potion> entry : REGISTRY.entrySet())
            if (entry.getValue() == potion) return entry.getKey();
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<Holder.Reference<T>> getRegistry(HolderOwner<T> owner) {
        return REGISTRY.entrySet().stream().map(entry -> {
            Holder.Reference<T> ref = Holder.Reference.createStandAlone(owner,
                    (ResourceKey<T>) ResourceKey.create(Registries.POTION, entry.getKey()));
            ((HolderReferenceAccessor<T>) ref).setValue((T) entry.getValue());
            return ref;
        }).toList();
    }
}
