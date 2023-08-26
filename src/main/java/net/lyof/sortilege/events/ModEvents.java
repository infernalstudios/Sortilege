package net.lyof.sortilege.events;

import net.lyof.sortilege.configs.ModCommonConfigs;
import net.lyof.sortilege.enchants.ModEnchants;
import net.lyof.sortilege.items.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber
public class ModEvents {
    @SubscribeEvent
    public static void xpBoost(PlayerXpEvent.XpChange event) {
        int amount = event.getAmount();
        float multiplier = 1;
        if (amount <= 0)
            return;

        if (event.getEntity().getItemBySlot(EquipmentSlot.HEAD).getItem() == ModItems.WITCH_HAT.get())
            multiplier += 0.4;

        if (multiplier != 1)
            event.setAmount(Math.round(amount * multiplier));
    }

    @SubscribeEvent
    public static void modCustomDrops(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        Level world = entity.getLevel();
        if (world.isClientSide) return;

        if (Objects.equals(event.getEntity().getEncodeId(), "minecraft:witch")) {
            if (Math.random() < ModCommonConfigs.WITCH_HAT_DROP.get()) {
                ItemEntity hat = new ItemEntity(world, entity.getX(), entity.getY(), entity.getZ(),
                        new ItemStack(ModItems.WITCH_HAT.get()));
                event.getEntity().level.addFreshEntity(hat);
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
}
