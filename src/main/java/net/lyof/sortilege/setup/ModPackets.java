package net.lyof.sortilege.setup;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lcc.sollib.core.Identifier;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.ModDataComponents;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.LapisShieldItem;
import net.lyof.sortilege.recipe.crafting.RecipeLock;
import net.lyof.sortilege.recipe.enchanting.catalyst.EnchantingCatalyst;
import net.lyof.sortilege.screen.custom.KnowledgeBookScreenHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ModPackets {
    public record InitializePacket() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<InitializePacket> TYPE = new Type<>(Sortilege.MOD.makeID("initialize"));
        public static final StreamCodec<FriendlyByteBuf, InitializePacket> STREAM_CODEC =
                StreamCodec.unit(new InitializePacket());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        @Environment(EnvType.CLIENT)
        public static void run(InitializePacket packet, ClientPlayNetworking.Context context) {
            ReloadListener.INSTANCE.reloadClient();
        }
    }

    public record InitializeEnchantPacket(Item item, List<Holder<Enchantment>> enchants) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<InitializeEnchantPacket> TYPE = new Type<>(Sortilege.MOD.makeID("initialize_enchant"));
        public static final StreamCodec<RegistryFriendlyByteBuf, InitializeEnchantPacket> STREAM_CODEC =
                StreamCodec.of((buf, packet) -> {
                    buf.writeResourceLocation(buf.registryAccess().registryOrThrow(Registries.ITEM).getKey(packet.item()));
                    buf.writeInt(packet.enchants().size());
                    packet.enchants().forEach(e -> buf.writeUtf(e.getRegisteredName()));
                }, buf -> {
                    HolderLookup.RegistryLookup<Enchantment> registry = buf.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                    Item key = buf.registryAccess().registryOrThrow(Registries.ITEM).get(buf.readResourceLocation());
                    int size = buf.readInt();
                    List<Holder<Enchantment>> enchants = new ArrayList<>(size);
                    for (int i = 0; i < size; i++)
                        registry.get(ResourceKey.create(Registries.ENCHANTMENT, Identifier.of(buf.readUtf()))).ifPresent(enchants::add);
                    return new InitializeEnchantPacket(key, enchants);
                });

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        @Environment(EnvType.CLIENT)
        public static void run(InitializeEnchantPacket packet, ClientPlayNetworking.Context context) {
            EnchantingCatalyst.register(packet.item(), packet.enchants());
        }
    }

    public record InitializeLockPacket(String advancement, boolean translatable, String name) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<InitializeLockPacket> TYPE = new Type<>(Sortilege.MOD.makeID("initialize_lock"));
        public static final StreamCodec<FriendlyByteBuf, InitializeLockPacket> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.STRING_UTF8, InitializeLockPacket::advancement,
                        ByteBufCodecs.BOOL, InitializeLockPacket::translatable,
                        ByteBufCodecs.STRING_UTF8, InitializeLockPacket::name,
                        InitializeLockPacket::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        @Environment(EnvType.CLIENT)
        public static void run(InitializeLockPacket packet, ClientPlayNetworking.Context context) {
            RecipeLock.read(packet);
        }
    }

    public record ParticlePacket(ResourceLocation particle, Vector3f pos, int color,
                                 int amount) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ParticlePacket> TYPE = new Type<>(Sortilege.MOD.makeID("particle_display"));
        public static final StreamCodec<FriendlyByteBuf, ParticlePacket> STREAM_CODEC =
                StreamCodec.composite(ResourceLocation.STREAM_CODEC, ParticlePacket::particle,
                        ByteBufCodecs.VECTOR3F, ParticlePacket::pos,
                        ByteBufCodecs.VAR_INT, ParticlePacket::color,
                        ByteBufCodecs.VAR_INT, ParticlePacket::amount,
                        ParticlePacket::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        @Environment(EnvType.CLIENT)
        public static void run(ParticlePacket packet, ClientPlayNetworking.Context context) {
            int spread = packet.amount() == 1 ? 0 : 2;
            ParticleType<?> particle = BuiltInRegistries.PARTICLE_TYPE.get(packet.particle());
    /*
            try {
                if (particle == BotaniaParticles.WISP) {
                    boolean depth = !ManaseerMonocleItem.hasMonocle(client.player);
                    float ar = r, ag = g, ab = b;

                    if (BotaniaConfig.client().subtlePowerSystem()) {
                        WispParticleData data = WispParticleData.wisp(0.1f, r, g, b, 0.5f, depth);
                        Proxy.INSTANCE.addParticleForceNear(client.player.level(), data, x, y, z,
                                (Math.random() - 0.5) * 0.02, (Math.random() - 0.5) * 0.02, (Math.random() - 0.5) * 0.02);
                    } else {
                        double luminance = 0.2126 * ar + 0.7152 * ag + 0.0722 * ab;

                        WispParticleData data;
                        if (luminance < 0.1) {
                            ar = r + (float) Math.random() * 0.125f;
                            ag = g + (float) Math.random() * 0.125f;
                            ab = b + (float) Math.random() * 0.125f;
                        }

                        float size = (float) (1 + (Math.random() - 0.5) * 0.065 + Math.sin(new Random(client.player.getUUID().getMostSignificantBits()).nextInt(9001)) * 0.4);
                        data = WispParticleData.wisp(0.2f * size, ar, ag, ab, 0.3f, depth);
                        Proxy.INSTANCE.addParticleForceNear(client.player.level(), data, x, y, z, 0, 0, 0);

                        data = WispParticleData.wisp(0.1f * size, r, g, b, 0.3f, depth);
                        client.player.level().addParticle(data, x, y, z, (float) (Math.random() - 0.5) * 0.06f, (float) (Math.random() - 0.5) * 0.06f, (float) (Math.random() - 0.5) * 0.06f);
                    }
                    return;
                }
            } catch (Throwable ignored) {}*/

            for (int i = 0; i < packet.amount(); i++) {
                context.client().level.addAlwaysVisibleParticle(particle instanceof ParticleOptions options
                                ? options
                                : ColorParticleOption.create((ParticleType<ColorParticleOption>) particle, packet.color()),
                        packet.pos().x() + (0.5 - Math.random()) * spread,
                        packet.pos().y() + (0.5 - Math.random()) * spread,
                        packet.pos().z() + (0.5 - Math.random()) * spread,
                        0, 0, 0);
            }
        }
    }

    public record LapisShieldPacket(int id, int cooldown) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<LapisShieldPacket> TYPE = new Type<>(Sortilege.MOD.makeID("lapis_shield_cooldown"));
        public static final StreamCodec<FriendlyByteBuf, LapisShieldPacket> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, LapisShieldPacket::id,
                        ByteBufCodecs.VAR_INT, LapisShieldPacket::cooldown,
                        LapisShieldPacket::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        @Environment(EnvType.CLIENT)
        public static void run(LapisShieldPacket packet, ClientPlayNetworking.Context context) {
            Entity e = context.client().level.getEntity(packet.id());
            if (!(e instanceof LivingEntity entity))
                return;

            ItemStack stack = entity.getOffhandItem();
            if (!ModConfig.lapisShieldEnabled.get() || !stack.is(ModItems.LAPIS_SHIELD)) return;

            if (packet.cooldown() == 0)
                LapisShieldItem.removeCooldown(stack);
            else
                LapisShieldItem.addCooldown(stack, packet.cooldown());
        }
    }

    public record KnowledgeBook(List<String> authors) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<KnowledgeBook> TYPE = new Type<>(Sortilege.MOD.makeID("knowledge_book_authors"));
        public static final StreamCodec<FriendlyByteBuf, KnowledgeBook> STREAM_CODEC =
                StreamCodec.of((buf, packet) -> {
                    buf.writeInt(packet.authors().size());
                    packet.authors().forEach(buf::writeUtf);
                }, buf -> {
                    int size = buf.readInt();
                    List<String> authors = new ArrayList<>(size);
                    for (int i = 0; i < size; i++)
                        authors.add(buf.readUtf());
                    return new KnowledgeBook(authors);
                });

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void run(KnowledgeBook packet, ServerPlayNetworking.Context context) {
            if (context.player().containerMenu instanceof KnowledgeBookScreenHandler screen)
                screen.stack.get(ModDataComponents.KNOWLEDGE).setAuthors(packet.authors());
        }
    }
}
