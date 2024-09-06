package net.lyof.sortilege.item.custom.potion;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AntidotePotionItem extends PotionItem {
    public AntidotePotionItem(FabricItemSettings settings) {
        super(settings);
    }

    public static void fillItemGroup(FabricItemGroupEntries entries, Item antidote) {
        for (Potion potion : PotionHelper.POTIONS.values())
            entries.add(PotionUtil.setPotion(antidote.getDefaultStack(), potion));
    }

    @Override
    public Text getName(ItemStack stack) {
        if (PotionUtil.getPotion(stack) == Potions.EMPTY)
            return Text.translatable(this.getTranslationKey());

        return Text.translatable(PotionUtil.getPotion(stack).getEffects().get(0).getTranslationKey())
                .append(" ")
                .append(Text.translatable(this.getTranslationKey()));
    }

    @Override
    public void appendTooltip(ItemStack itemstack, @Nullable World level, List<Text> list, TooltipContext context) {
        if (PotionUtil.getPotion(itemstack) == Potions.EMPTY)
            return;

        MutableText desc = Text.translatable("sortilege.antidote.cures").formatted(Formatting.DARK_PURPLE)
                .append(" ");

        StatusEffectInstance effect = PotionUtil.getPotion(itemstack).getEffects().get(0);
        if (effect.getEffectType().isBeneficial())
            desc.append(Text.translatable(effect.getTranslationKey()).formatted(Formatting.BLUE));
        else
            desc.append(Text.translatable(effect.getTranslationKey()).formatted(Formatting.RED));

        list.add(desc);
    }

    @Override
    public ItemStack finishUsing(ItemStack itemstack, World level, LivingEntity entity) {
        PlayerEntity player = entity instanceof PlayerEntity ? (PlayerEntity) entity : null;
        if (player instanceof ServerPlayerEntity)
            Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity) player, itemstack);

        if (!level.isClient()) {
            StatusEffect effect = PotionUtil.getPotionEffects(itemstack).get(0).getEffectType();

            if (player != null && player.hasStatusEffect(effect)) {
                player.removeStatusEffect(effect);
                // todo: particles?
            }
        }

        if (player != null) {
            player.incrementStat(Stats.USED.getOrCreateStat(this));
            if (!player.getAbilities().creativeMode)
                itemstack.decrement(1);
        }

        if (player == null || !player.getAbilities().creativeMode) {
            if (itemstack.isEmpty())
                return new ItemStack(Items.GLASS_BOTTLE);

            if (player != null)
                player.getInventory().offerOrDrop(new ItemStack(Items.GLASS_BOTTLE));
        }

        entity.emitGameEvent(GameEvent.DRINK);
        return itemstack;
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getStackInHand(hand);

        if (PotionUtil.getPotion(itemstack) != Potions.EMPTY && player.isSneaking() ||
                player.hasStatusEffect(PotionUtil.getPotionEffects(itemstack).get(0).getEffectType()))

            return ItemUsage.consumeHeldItem(level, player, hand);

        return TypedActionResult.fail(itemstack);
    }

    @Override
    public int getMaxUseTime(ItemStack itemstack) {
        return super.getMaxUseTime(itemstack) / 2;
    }

    public static int getItemColor(ItemStack itemstack, int i) {
        if (i == 0) {
            return PotionUtil.getColor(itemstack);
        }
        return -1;
    }
}
