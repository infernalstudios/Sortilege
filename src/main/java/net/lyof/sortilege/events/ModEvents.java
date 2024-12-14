package net.lyof.sortilege.events;

import net.lyof.sortilege.configs.ConfigEntries;
import net.lyof.sortilege.enchants.ModEnchants;
import net.lyof.sortilege.items.ModItems;
import net.lyof.sortilege.particles.ModParticles;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.utils.ItemHelper;
import net.lyof.sortilege.utils.XPHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.antlr.v4.runtime.misc.Triple;

import java.util.Objects;

@Mod.EventBusSubscriber
public class ModEvents {
    @SubscribeEvent
    public static void xpBoost(LivingExperienceDropEvent event) {
        if (event.getEntity() instanceof Player && ConfigEntries.doXPKeep) {
            event.setDroppedExperience(0);
            return;
        }

        if (event.getAttackingPlayer() == null)
            return;
        if (event.getAttackingPlayer().getItemBySlot(EquipmentSlot.HEAD).getItem() == ModItems.WITCH_HAT.get())
            event.setDroppedExperience(event.getDroppedExperience() + ConfigEntries.witchHatBonus);

        if (Math.random() < ConfigEntries.bountyChance && event.getEntity() instanceof Monster monster
                && ((ConfigEntries.bountyWhitelist && monster.getType().is(ModTags.Entities.BOUNTIES))
                    || (!ConfigEntries.bountyWhitelist && !monster.getType().is(ModTags.Entities.BOUNTIES)))) {

            //XPHelper.dropxpPinata(monster.getLevel(), monster.getX(), monster.getY(), monster.getZ(), ConfigEntries.BountyValue);
            if (monster.getLevel() instanceof ServerLevel server)
                ExperienceOrb.award(server, monster.position(), ConfigEntries.bountyValue);
            else {
                ModParticles.spawnWisps(event.getAttackingPlayer().getLevel(), monster.getX(), monster.getY() + monster.getEyeHeight() / 2, monster.getZ(),
                        8, new Triple<>(0.5f, 1f, 0.2f));
            }
        }
    }

    @SubscribeEvent
    public static void xpSave(LivingDeathEvent event) {
        if (!(event.getEntity().getLevel() instanceof ServerLevel server) || !ConfigEntries.doXPKeep
                || event.getEntity().getLevel().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY))
            return;

        // Player got killed
        if (event.getEntity() instanceof Player player) {
            // Split the xp
            int safe_xp = (int) Math.round(XPHelper.getTotalxp(player, server) * ConfigEntries.selfXPRatio);
            int steal_xp = (int) Math.round(XPHelper.getTotalxp(player, server) * ConfigEntries.attackerXPRatio);
            int drop_xp = (int) Math.round(XPHelper.getTotalxp(player, server) * ConfigEntries.dropXPRatio);

            // Save a part for respawn
            XPHelper.setSavedXp(player, safe_xp);

            // If indirect death: add steal to drop
            if (event.getSource().getEntity() == null || !(event.getSource().getEntity() instanceof LivingEntity entity)) {
                drop_xp += steal_xp;
                steal_xp = 0;
            }

            // If killed by player: add steal to drop or give to attacker
            else if (event.getSource().getEntity() instanceof Player attacker) {
                if (!ConfigEntries.stealFromPlayers) {
                    drop_xp += steal_xp;
                    steal_xp = 0;
                }
                else {
                    attacker.giveExperiencePoints(steal_xp);
                }
            }

            // If killed by a living entity: make it a bounty
            else {
                XPHelper.setSavedXp(entity, steal_xp, true);
                // Temporary: custom effect or something
                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 999999));
            }

            // Drop the last part
            //XPHelper.dropxpPinata(player.getLevel(), player.getX(), player.getY(), player.getZ(), drop_xp);
            ExperienceOrb.award(server, player.position(), drop_xp);
        }

        // Bounty got killed
        else if (XPHelper.getSavedXp(event.getEntity()) > 0) {
            LivingEntity entity = event.getEntity();

            // Probably better: ExperienceOrb.award(server, entity.position(), amount);
            //XPHelper.dropxpPinata(entity.getLevel(), entity.getX(), entity.getY(), entity.getZ(),
            //        XPHelper.getSavedXp(event.getEntity()));
            ExperienceOrb.award(server, entity.position(), XPHelper.getSavedXp(event.getEntity()));

            // Remove the bounty
            XPHelper.removeSavedXp(event.getEntity());
        }
    }
    
    @SubscribeEvent
    public static void xpRefill(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();

        if (player.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || !ConfigEntries.doXPKeep)
            return;

        var xp = XPHelper.getSavedXp(player);
        if (xp > 0) {
            player.giveExperiencePoints(xp);
            XPHelper.removeSavedXp(player);
        }
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
            if (Math.random() < ConfigEntries.witchHatDropChance) {
                ItemStack hat = new ItemStack(ModItems.WITCH_HAT.get());
                hat.setDamageValue((int) Math.round(Math.random() * (hat.getMaxDamage() - 10)) + 10);
                event.getEntity().level.addFreshEntity(new ItemEntity(world, entity.getX(), entity.getY(), entity.getZ(), hat));
            }
        }
    }

    @SubscribeEvent
    public static void modCustomAttacks(LivingAttackEvent event) {
        if (event.getSource().isFall() &&
                ItemHelper.getEnchantLevel(Enchantments.FALL_PROTECTION, event.getEntity().getItemBySlot(EquipmentSlot.FEET)) >=
                        ConfigEntries.betterFeatherFalling)
            event.setCanceled(true);

        if (event.getSource().isFire() &&
                ItemHelper.getEnchantLevel(Enchantments.FIRE_PROTECTION, event.getEntity().getItemBySlot(EquipmentSlot.FEET)) >=
                        ConfigEntries.betterFireProt &&
                ItemHelper.getEnchantLevel(Enchantments.FIRE_PROTECTION, event.getEntity().getItemBySlot(EquipmentSlot.LEGS)) >=
                        ConfigEntries.betterFireProt &&
                ItemHelper.getEnchantLevel(Enchantments.FIRE_PROTECTION, event.getEntity().getItemBySlot(EquipmentSlot.CHEST)) >=
                        ConfigEntries.betterFireProt &&
                ItemHelper.getEnchantLevel(Enchantments.FIRE_PROTECTION, event.getEntity().getItemBySlot(EquipmentSlot.HEAD)) >=
                        ConfigEntries.betterFireProt)
            event.setCanceled(true);

        if (ConfigEntries.betterMagicProt && Math.random() <=
                0.05 * EnchantmentHelper.getEnchantmentLevel(ModEnchants.MAGIC_PROTECTION.get(), event.getEntity()))
            event.setCanceled(true);

        Entity sourceentity = event.getSource().getEntity();
        if (sourceentity == null) return;
        if (!(sourceentity instanceof LivingEntity source)) return;
        if (sourceentity.level.isClientSide) return;

        if (EnchantmentHelper.getEnchantmentLevel(ModEnchants.ARCANE.get(), source) > 0)
            event.getSource().setMagic();
    }
}
