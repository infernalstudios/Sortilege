package net.lyof.sortilege.particle;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.setup.ModPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class ModParticles {
    public static void spawnWisps(Level world, double x, double y, double z, int amount, float[] color) {
        if (!world.isClientSide()) {
            FriendlyByteBuf buf = PacketByteBufs.create();

            buf.writeDouble(x);
            buf.writeDouble(y);
            buf.writeDouble(z);
            buf.writeFloat(Math.max(0, color[0]));
            buf.writeFloat(Math.max(0, color[1]));
            buf.writeFloat(Math.max(0, color[2]));
            buf.writeInt(amount);

            for (ServerPlayer player : PlayerLookup.tracking((ServerLevel) world, new BlockPos((int) x, (int) y, (int) z))) {
                ServerPlayNetworking.send(player, ModPackets.WISP_PARTICLE_DISPLAY, buf);
            }
        }
    }


    public static void register() {}

    public static final SimpleParticleType WISP_PIXEL = Registry.register(BuiltInRegistries.PARTICLE_TYPE, Sortilege.makeID("wisp_pixel"),
            FabricParticleTypes.simple());
}
