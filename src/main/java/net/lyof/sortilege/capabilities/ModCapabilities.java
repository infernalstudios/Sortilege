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
     *
     * @implNote Sortilege only adds one capability to store XP data, so we are unconditionally transferring all data.
     *     If you want to add more capabilities, you should add a check for {@link PlayerEvent.Clone#isWasDeath()} so
     *     that the data persists if the player is cloned due to leaving The End through the dragon portal.
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayerNew && event.getOriginal() instanceof ServerPlayer serverPlayerOld) {
            // since we want the new player to have the same XP number as the old one, we are unconditionally transferring all data
            serverPlayerOld.reviveCaps();
            EntityXpStorage.ifPresent(serverPlayerNew, capabilityNew -> EntityXpStorage.ifPresent(serverPlayerOld, capabilityOld -> capabilityNew.load(capabilityOld.save(new CompoundTag()))));
            serverPlayerOld.invalidateCaps();

            // uncomment the below line and add other capabilites after it if you want to add more
            // see the @implNote above for why
            //if (!event.isWasDeath()) return
        }
    }

}
