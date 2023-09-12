package net.lyof.sortilege.events;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.ConfigEntry;
import net.lyof.sortilege.configs.ModJsonConfigs;
import net.lyof.sortilege.enchants.ModEnchants;
import net.lyof.sortilege.items.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber
public class ModEvents {
    public static final ConfigEntry<Double> WITCH_HAT_DROP_CHANCE =
            new ConfigEntry<>("witch_hat.drop_chance", 0.1);
    public static final ConfigEntry<Integer> WITCH_HAT_BONUS =
            new ConfigEntry<>("witch_hat.xp_bonus", 3);

    @SubscribeEvent
    public static void xpBoost(LivingExperienceDropEvent event) {
        if (event.getAttackingPlayer() == null)
            return;
        if (event.getAttackingPlayer().getItemBySlot(EquipmentSlot.HEAD).getItem() == ModItems.WITCH_HAT.get())
            event.setDroppedExperience(event.getOriginalExperience() + WITCH_HAT_BONUS.get());
    }

    @SubscribeEvent
    public static void modCustomDrops(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        Level world = entity.getLevel();
        if (world.isClientSide) return;

        if (Objects.equals(event.getEntity().getEncodeId(), "minecraft:witch")) {
            if (Math.random() < WITCH_HAT_DROP_CHANCE.get()) {
                ItemStack hat = new ItemStack(ModItems.WITCH_HAT.get());
                hat.setDamageValue((int) Math.round(Math.random() * (hat.getMaxDamage() - 10)) + 10);
                event.getEntity().level.addFreshEntity(new ItemEntity(world, entity.getX(), entity.getY(), entity.getZ(), hat));
            }
        }
    }

    @SubscribeEvent
    public static void modCustomAttacks(LivingAttackEvent event) {
        Entity sourceentity = event.getSource().getEntity();
        if (sourceentity == null) return;
        if (!(sourceentity instanceof LivingEntity source)) return;
        if (sourceentity.level.isClientSide) return;

        if (EnchantmentHelper.getEnchantmentLevel(ModEnchants.ARCANE.get(), source) > 0)
            event.getSource().setMagic();
    }

    @SubscribeEvent
    public static void reloadConfigs(PlayerEvent.PlayerLoggedInEvent event) {
        ModJsonConfigs.register();
    }
}
