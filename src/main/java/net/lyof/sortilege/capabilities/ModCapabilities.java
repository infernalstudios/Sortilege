package net.lyof.sortilege.capabilities;

import net.lyof.sortilege.capabilities.entity.EntityXpStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ModCapabilities {
    @SubscribeEvent
    public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        EntityXpStorage.attach(event);
    }

    /**
     * Clones player capabilities along with the new player.
     *
     * @param event The player clone event.
     * @see <a
     *     href="https://github.com/MinecraftForge/MinecraftForge/issues/9311">MinecraftForge/MinecraftForge#9311</a>
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayerNew && event.getOriginal() instanceof ServerPlayer serverPlayerOld) {
            if (event.isWasDeath()) return;

            serverPlayerOld.reviveCaps();
            EntityXpStorage.ifPresent(serverPlayerNew, capabilityNew -> EntityXpStorage.ifPresent(serverPlayerOld, capabilityOld -> capabilityNew.load(capabilityNew.save(new CompoundTag()))));
            serverPlayerOld.invalidateCaps();
        }
    }

}
