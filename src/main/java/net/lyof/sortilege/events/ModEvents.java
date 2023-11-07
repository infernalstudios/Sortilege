package net.lyof.sortilege.events;

import com.mojang.datafixers.util.Pair;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.ConfigEntries;
import net.lyof.sortilege.enchants.ModEnchants;
import net.lyof.sortilege.items.ModItems;
import net.lyof.sortilege.utils.XPHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
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

import java.util.Objects;

@Mod.EventBusSubscriber
public class ModEvents {
    @SubscribeEvent
    public static void xpBoost(LivingExperienceDropEvent event) {
        if (event.getEntity() instanceof Player && ConfigEntries.DoXPBounty) {
            event.setDroppedExperience(0);
            return;
        }

        if (event.getAttackingPlayer() == null)
            return;
        if (event.getAttackingPlayer().getItemBySlot(EquipmentSlot.HEAD).getItem() == ModItems.WITCH_HAT.get())
            event.setDroppedExperience(event.getDroppedExperience() + ConfigEntries.WitchHatBonus);
    }

    @SubscribeEvent
    public static void xpSave(LivingDeathEvent event) {
        if (!(event.getEntity().getLevel() instanceof ServerLevel server) || !ConfigEntries.DoXPBounty)
            return;

        if (event.getEntity() instanceof Player player) {
            int safe_xp = (int) Math.round(XPHelper.getTotalxp(player, server) * ConfigEntries.SelfXPRatio);
            int stolen_xp = (int) Math.round(XPHelper.getTotalxp(player, server) * ConfigEntries.AttackerXPRatio);

            if (XPHelper.XP_SAVES.containsKey(player.getStringUUID()))
                XPHelper.XP_SAVES.replace(player.getStringUUID(), safe_xp);
            else
                XPHelper.XP_SAVES.put(player.getStringUUID(), safe_xp);
            
            if (event.getSource().getEntity() == null || !(event.getSource().getEntity() instanceof LivingEntity entity))
                return;

            XPHelper.XP_SAVES.putIfAbsent(entity.getStringUUID(), stolen_xp);
            entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 999999));
        }
        
        else if (XPHelper.XP_SAVES.containsKey(event.getEntity().getStringUUID())) {
            LivingEntity entity = event.getEntity();
            Sortilege.log("Retrieving " + XPHelper.XP_SAVES.get(entity.getStringUUID()) + " xp points!");

            // Probably better: ExperienceOrb.award(server, entity.position(), amount);
            for (int i = 0; i < XPHelper.XP_SAVES.get(entity.getStringUUID()) / 3; i++) {
                entity.getLevel().addFreshEntity(new ExperienceOrb(
                        entity.getLevel(),
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        3));
            }

            XPHelper.XP_SAVES.remove(entity.getStringUUID());
        }
    }
    
    @SubscribeEvent
    public static void xpRefill(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        Sortilege.log(XPHelper.XP_SAVES);

        if (player.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || !ConfigEntries.DoXPBounty)
            return;

        player.giveExperiencePoints(XPHelper.XP_SAVES.get(player.getStringUUID()));
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
