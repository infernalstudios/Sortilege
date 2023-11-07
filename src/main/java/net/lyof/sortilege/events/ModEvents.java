package net.lyof.sortilege.events;

import com.mojang.datafixers.util.Pair;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.ConfigEntries;
import net.lyof.sortilege.enchants.ModEnchants;
import net.lyof.sortilege.items.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber
public class ModEvents {
    public static Map<String, Pair<Integer, Float>> PLAYER_XPS = new HashMap<>();

    @SubscribeEvent
    public static void xpBoost(LivingExperienceDropEvent event) {
        if (event.getEntity() instanceof Player player) {
            float ratio = 0.5f;

            Pair<Integer, Float> xp =
                    new Pair<>(Math.round(player.experienceLevel * ratio), player.experienceProgress * ratio);

            if (PLAYER_XPS.containsKey(player.getStringUUID()))
                PLAYER_XPS.replace(player.getStringUUID(), xp);
            else
                PLAYER_XPS.put(player.getStringUUID(), xp);

            event.setDroppedExperience(Math.round(event.getDroppedExperience() * (1 - ratio)));
        }

        if (event.getAttackingPlayer() == null)
            return;
        if (event.getAttackingPlayer().getItemBySlot(EquipmentSlot.HEAD).getItem() == ModItems.WITCH_HAT.get())
            event.setDroppedExperience(event.getDroppedExperience() + ConfigEntries.WitchHatBonus);
    }

    @SubscribeEvent
    public static void xpRefill(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        Sortilege.log(PLAYER_XPS);

        if (player.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY))
            return;

        player.experienceLevel = PLAYER_XPS.getOrDefault(player.getStringUUID(), new Pair<>(0, 0f)).getFirst();
        player.experienceProgress = PLAYER_XPS.getOrDefault(player.getStringUUID(), new Pair<>(0, 0f)).getSecond();
    }

    @SubscribeEvent
    public static void xpCap(PlayerXpEvent.XpChange event) {
        Player player = event.getEntity();
        if (ConfigEntries.xpLevelCap > -1 && player.experienceLevel >= ConfigEntries.xpLevelCap) {
            player.experienceLevel = ConfigEntries.xpLevelCap;
            if (event.getAmount() > 0)
                event.setAmount(0);
        }
    }

    @SubscribeEvent
    public static void modCustomDrops(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        Level world = entity.getLevel();
        if (world.isClientSide) return;

        if (Objects.equals(event.getEntity().getEncodeId(), "minecraft:witch")) {
            if (Math.random() < ConfigEntries.WitchHatDropChance) {
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
}
