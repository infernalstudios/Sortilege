package net.lyof.sortilege.item.custom;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.custom.potion.PotionShenanigans;
import net.lyof.sortilege.particle.ModParticles;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AntidotePotionItem extends PotionItem {
    public AntidotePotionItem(FabricItemSettings settings) {
        super(settings);
    }

    public static void fillItemGroup(FabricItemGroupEntries entries, Item antidote) {
        if (!ConfigEntries.antidoteEnabled) return;

        for (Potion potion : PotionHelper.POTIONS.values())
            entries.add(PotionUtil.setPotion(antidote.getDefaultStack(), potion));
    }

    @Override
    public Text getName(ItemStack stack) {
        if (PotionUtil.getPotion(stack).getEffects().size() <= 0)
            return Text.translatable(this.getTranslationKey());

        return Text.translatable(PotionUtil.getPotion(stack).getEffects().get(0).getTranslationKey())
                .append(" ")
                .append(Text.translatable(this.getTranslationKey()));
    }

    @Override
    public void appendTooltip(ItemStack itemstack, @Nullable World level, List<Text> list, TooltipContext context) {
        if (PotionUtil.getPotion(itemstack).getEffects().isEmpty())
            return;

        MutableText desc = Text.translatable("item.sortilege.antidote.cures").formatted(Formatting.DARK_PURPLE)
                .append(" ");

        StatusEffectInstance effect = PotionUtil.getPotion(itemstack).getEffects().get(0);
        if (effect.getEffectType().isBeneficial())
            desc.append(Text.translatable(effect.getTranslationKey()).formatted(Formatting.BLUE));
        else
            desc.append(Text.translatable(effect.getTranslationKey()).formatted(Formatting.RED));

        list.add(desc);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        PlayerEntity player = entity instanceof PlayerEntity ? (PlayerEntity) entity : null;
        if (player instanceof ServerPlayerEntity)
            Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity) player, stack);

        if (!world.isClient()) {
            StatusEffect effect = PotionUtil.getPotionEffects(stack).get(0).getEffectType();

            if (entity.hasStatusEffect(effect)) {
                entity.removeStatusEffect(effect);
                int color = PotionUtil.getColor(stack);
                float r = ColorHelper.Argb.getRed(color) / 255f,
                      g = ColorHelper.Argb.getGreen(color) / 255f,
                      b = ColorHelper.Argb.getBlue(color) / 255f;
                ModParticles.spawnWisps(world, entity.getX(), entity.getEyeY(), entity.getZ(), 16, new float[]{r, g, b});
            }
            if (ConfigEntries.antidoteImmunityTime > 0)
                ((PotionShenanigans) entity).sorti$setImmunity(effect, ConfigEntries.antidoteImmunityTime * 20);
        }

        if (player != null) {
            player.incrementStat(Stats.USED.getOrCreateStat(this));
            if (!player.getAbilities().creativeMode)
                stack.decrement(1);
        }

        if (player == null)
            return stack;
        if (!player.getAbilities().creativeMode) {
            if (stack.isEmpty())
                return new ItemStack(Items.GLASS_BOTTLE);

            player.getInventory().offerOrDrop(new ItemStack(Items.GLASS_BOTTLE));
        }

        entity.emitGameEvent(GameEvent.DRINK);
        return stack;
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getStackInHand(hand);

        if (!PotionUtil.getPotion(itemstack).getEffects().isEmpty() && (player.isSneaking() ||
                player.hasStatusEffect(PotionUtil.getPotionEffects(itemstack).get(0).getEffectType())))

            return ItemUsage.consumeHeldItem(level, player, hand);

        return TypedActionResult.fail(itemstack);
    }

    @Override
    public int getMaxUseTime(ItemStack itemstack) {
        return super.getMaxUseTime(itemstack) / 2;
    }

    public static int getItemColor(ItemStack itemstack, int i) {
        if (i == 0)
            return PotionUtil.getColor(itemstack);
        return -1;
    }
}
