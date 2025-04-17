package net.lyof.sortilege.particle;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.setup.ModPackets;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ModParticles {
    public static void spawnWisps(World world, double x, double y, double z, int amount, float[] color) {
        /*level.addParticle(WISP_PIXEL, x + (0.5 - Math.random()) * spread,
                y + (0.5 - Math.random()) * spread,
                z + (0.5 - Math.random()) * spread,
                color[0], Math.max(0, color[1]), Math.max(0, color[2]));*/

        if (!world.isClient()) {
            PacketByteBuf buf = PacketByteBufs.create();

            buf.writeDouble(x);
            buf.writeDouble(y);
            buf.writeDouble(z);
            buf.writeFloat(Math.max(0, color[0]));
            buf.writeFloat(Math.max(0, color[1]));
            buf.writeFloat(Math.max(0, color[2]));
            buf.writeInt(amount);

            for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, new BlockPos((int) x, (int) y, (int) z))) {
                ServerPlayNetworking.send(player, ModPackets.WISP_PARTICLE_DISPLAY, buf);
            }
        }
    }


    public static void register() {}

    public static final DefaultParticleType WISP_PIXEL = Registry.register(Registries.PARTICLE_TYPE, Sortilege.makeID("wisp_pixel"),
            FabricParticleTypes.simple());
}
