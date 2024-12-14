package net.lyof.sortilege.capabilities.entity;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.capabilities.CapabilityProvider;
import net.lyof.sortilege.capabilities.IPersistentCapability;
import net.lyof.sortilege.capabilities.ISyncedPersistentCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EntityXpStorage implements IPersistentCapability<EntityXpStorage> {
    /* ATTACHMENT */

    public static final Capability<EntityXpStorage> INSTANCE = CapabilityManager.get(new CapabilityToken<>() { });

    public static void attach(AttachCapabilitiesEvent<? extends Entity> event) {
        if (!(event.getObject() instanceof LivingEntity entity)) return;

        event.addCapability(new ResourceLocation(Sortilege.MOD_ID, "entity_xp_storage"), new CapabilityProvider<>(new EntityXpStorage(entity)));
    }


    /* ENTITY */

    private final LivingEntity entity;

    public EntityXpStorage(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }


    /* XP STORAGE */

    private int xp = 0;

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = Math.max(0, xp);
    }

    public void setXp(int xp, boolean onlyIfZero) {
        if (onlyIfZero && this.xp != 0) return;

        this.setXp(xp);
    }


    /* SERIALIZATION */

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("XP", this.xp);
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        this.xp = tag.getInt("XP");
    }


    /* CAPABILITY */

    @Override
    public Capability<EntityXpStorage> getDefaultInstance() {
        return INSTANCE;
    }

    public static Optional<EntityXpStorage> get(LivingEntity entity) {
        return entity.getCapability(INSTANCE).resolve();
    }

    public static boolean isPresent(LivingEntity entity) {
        if (entity == null) return false;

        return get(entity).isPresent();
    }

    public static void ifPresent(LivingEntity entity, Consumer<EntityXpStorage> action) {
        if (entity == null) return;

        get(entity).ifPresent(action);
    }

    public static void ifPresentOrElse(LivingEntity entity, Consumer<EntityXpStorage> action, Runnable orElse) {
        if (entity == null) return;

        get(entity).ifPresentOrElse(action, orElse);
    }

    public static <R> R getIfPresent(LivingEntity entity, Function<EntityXpStorage, R> action, Supplier<R> orElse) {
        if (entity != null) {
            var capability = get(entity);
            if (capability.isPresent()) return action.apply(capability.get());
        }

        return orElse.get();
    }
}
