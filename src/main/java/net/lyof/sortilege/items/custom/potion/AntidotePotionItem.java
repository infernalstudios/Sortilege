package net.lyof.sortilege.items.custom.potion;

import net.lyof.sortilege.utils.PotionHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AntidotePotionItem extends PotionItem {
    public AntidotePotionItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void fillItemCategory(CreativeModeTab pTab, NonNullList<ItemStack> pList) {
        for (Potion potion : PotionHelper.POTIONS.values()) {
            if (potion.allowedInCreativeTab(this, pTab, this.allowedIn(pTab)))
                pList.add(PotionUtils.setPotion(new ItemStack(this), potion));
        }
    }

    @Override
    public Component getName(ItemStack itemstack) {
        if (PotionUtils.getPotion(itemstack) == Potions.EMPTY)
            return Component.translatable(this.getDescriptionId());

        return Component.translatable(PotionUtils.getPotion(itemstack).getEffects().get(0).getDescriptionId())
                .append(" ")
                .append(Component.translatable(this.getDescriptionId()));
    }

    @Override
    public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        if (PotionUtils.getPotion(itemstack) == Potions.EMPTY)
            return;

        MutableComponent desc = Component.translatable("sortilege.antidote.cures").withStyle(ChatFormatting.DARK_PURPLE)
                .append(" ");

        MobEffectInstance effect = PotionUtils.getPotion(itemstack).getEffects().get(0);
        if (effect.getEffect().isBeneficial())
            desc.append(Component.translatable(effect.getDescriptionId()).withStyle(ChatFormatting.BLUE));
        else
            desc.append(Component.translatable(effect.getDescriptionId()).withStyle(ChatFormatting.RED));

        list.add(desc);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, Level level, LivingEntity entity) {
        Player player = entity instanceof Player ? (Player)entity : null;
        if (player instanceof ServerPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)player, itemstack);
        }

        if (!level.isClientSide) {
            MobEffect effect = PotionUtils.getMobEffects(itemstack).get(0).getEffect();

            if (player != null && player.hasEffect(effect)) {
                player.removeEffect(effect);
                // todo: particles?
            }
        }

        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
        }

        if (player == null || !player.getAbilities().instabuild) {
            if (itemstack.isEmpty()) {
                return new ItemStack(Items.GLASS_BOTTLE);
            }

            if (player != null) {
                player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }
        }

        entity.gameEvent(GameEvent.DRINK);
        return itemstack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (PotionUtils.getPotion(itemstack) != Potions.EMPTY && player.isCrouching() ||
                player.hasEffect(PotionUtils.getMobEffects(itemstack).get(0).getEffect()))

            return ItemUtils.startUsingInstantly(level, player, hand);

        return InteractionResultHolder.fail(itemstack);
    }

    @Override
    public int getUseDuration(ItemStack itemstack) {
        return super.getUseDuration(itemstack) / 2;
    }

    public static int getItemColor(ItemStack itemstack, int i) {
        if (i == 0) {
            return PotionUtils.getColor(itemstack);
        }
        return -1;
    }
/*
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (!(self.getItem() instanceof AntidotePotionItem && other.getItem() instanceof AntidotePotionItem))
            return false;
        if (PotionUtils.getPotion(self) == Potions.EMPTY || PotionUtils.getPotion(other) == Potions.EMPTY)
            return false;

        MobEffect selfeffect = PotionUtils.getPotion(self).getEffects().get(0).getEffect();
        MobEffect othereffect = PotionUtils.getPotion(other).getEffects().get(0).getEffect();
        return (boolean) Sortilege.log(selfeffect == othereffect);
    }
/*
    @Override
    public boolean overrideStackedOnOther(ItemStack itemstack, Slot slot, ClickAction action, Player player) {
        if (!(itemstack.getItem() instanceof AntidotePotionItem && slot.getItem().getItem() instanceof AntidotePotionItem))
            return false;
        if (PotionUtils.getPotion(itemstack) == Potions.EMPTY || PotionUtils.getPotion(slot.getItem()) == Potions.EMPTY)
            return false;

        MobEffect selfeffect = PotionUtils.getPotion(itemstack).getEffects().get(0).getEffect();
        MobEffect othereffect = PotionUtils.getPotion(slot.getItem()).getEffects().get(0).getEffect();
        Sortilege.log(selfeffect == othereffect);
        return selfeffect == othereffect;
    }*/
}
