package net.lyof.sortilege.item.potion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.lcc.sollib.core.Identifier;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.mixin.accessor.HolderReferenceAccessor;
import net.lyof.sortilege.setup.ModConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CustomPotionData implements CustomPacketPayload {
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
            INSTANCES.add(new CustomPotionData(Identifier.of(json.get("potion").getAsString()),
                    json.has("effects") && json.get("effects").isJsonArray() ?
                            readEffectList(json.get("effects").getAsJsonArray()) : null,
                    json.has("drinking_time") ?
                            json.get("drinking_time").getAsInt() : ModConfig.potionDrinkingTime.get(),
                    json.has("cooldown") ?
                            json.get("cooldown").getAsInt() : ModConfig.potionCooldown.get(),
                    json.has("stack_size") ?
                            json.get("stack_size").getAsInt() : ModConfig.potionStackSize.get(),
                    json.has("create") && json.get("create").getAsBoolean()));
        }
    }

    public static List<MobEffectInstance> readEffectList(JsonArray json) {
        List<MobEffectInstance> effects = new ArrayList<>();
        for (JsonElement e : json) {
            if (!e.isJsonObject()) continue;
            JsonObject o = e.getAsJsonObject();
            if (!o.has("effect") || !o.has("duration") || !o.has("amplifier")) continue;

            Optional<Holder.Reference<MobEffect>> effect = BuiltInRegistries.MOB_EFFECT.getHolder(Identifier.of(o.get("effect").getAsString()));
            if (effect.isEmpty()) continue;
            int duration = o.get("duration").getAsInt();
            int amplifier = o.get("amplifier").getAsInt();

            effects.add(new MobEffectInstance(effect.get(), duration, amplifier));
        }
        return effects;
    }

    public void read(ClientPlayNetworking.Context context) {
        INSTANCES.add(this);
    }

    public static CustomPotionData read(FriendlyByteBuf packet) {
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
            effects = new ArrayList<>(size);
            for (int j = 0; j < size; j++) {
                Optional<Holder.Reference<MobEffect>> effect = BuiltInRegistries.MOB_EFFECT.getHolder(packet.readResourceLocation());
                int duration = packet.readInt();
                int amplifier = packet.readInt();

                effect.ifPresent(e -> effects.add(new MobEffectInstance(e, duration, amplifier)));
            }
        }

        return new CustomPotionData(potion, effects, drinkingTime, cooldown, stackSize, create);
    }

    public static void write(List<CustomPacketPayload> packets) {
        packets.addAll(INSTANCES);
    }

    public static void write(FriendlyByteBuf packet, CustomPotionData data) {
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
                packet.writeResourceLocation(BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()));
                packet.writeInt(effect.getDuration());
                packet.writeInt(effect.getAmplifier());
            }
        }
    }

    public static final CustomPacketPayload.Type<CustomPotionData> TYPE = new Type<>(Sortilege.MOD.makeID("initialize_potion"));
    public static final StreamCodec<FriendlyByteBuf, CustomPotionData> STREAM_CODEC =
            StreamCodec.of(CustomPotionData::write, CustomPotionData::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    private static final List<CustomPotionData> INSTANCES = new ArrayList<>();
    private static final Map<Holder<Potion>, CustomPotionData> CACHE = new HashMap<>();

    @Nullable
    public static CustomPotionData get(Holder<Potion> potion) {
        if (potion == null) return null;
        if (!ModConfig.customPotionData.get()) return null;

        if (CACHE.containsKey(potion)) return CACHE.get(potion);
        CustomPotionData result = INSTANCES.stream()
                .filter(data -> data.potion.equals(potion.unwrapKey().map(ResourceKey::location).orElse(null)))
                .findFirst().orElse(null);
        CACHE.put(potion, result);
        return result;
    }

    @Nullable
    public static CustomPotionData get(PotionContents potion) {
        if (potion == null) return null;
        return get(potion.potion().orElse(null));
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
    public static <T> Collection<Holder.Reference<T>> getRegistry(HolderOwner<T> owner) {
        return REGISTRY.entrySet().stream().map(entry -> {
            Holder.Reference<T> ref = Holder.Reference.createStandAlone(owner,
                    (ResourceKey<T>) ResourceKey.create(Registries.POTION, entry.getKey()));
            ((HolderReferenceAccessor<T>) ref).setValue((T) entry.getValue());
            return ref;
        }).toList();
    }
}
