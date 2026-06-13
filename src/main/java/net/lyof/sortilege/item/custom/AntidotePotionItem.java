package net.lyof.sortilege.item.custom;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.item.custom.potion.PotionShenanigans;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AntidotePotionItem extends PotionItem {
    public AntidotePotionItem(FabricItemSettings settings) {
        super(settings);
    }

    public static void fillItemGroup(FabricItemGroupEntries entries, Item antidote) {
        if (!ModConfig.antidoteEnabled.get()) return;

        for (Potion potion : PotionHelper.POTIONS.values())
            entries.accept(PotionUtils.setPotion(antidote.getDefaultInstance(), potion));
    }

    @Override
    public Component getName(ItemStack stack) {
        if (PotionUtils.getPotion(stack).getEffects().size() <= 0)
            return Component.translatable(this.getDescriptionId());

        return Component.translatable(PotionUtils.getPotion(stack).getEffects().get(0).getDescriptionId())
                .append(" ")
                .append(Component.translatable(this.getDescriptionId()));
    }

    @Override
    public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag context) {
        if (PotionUtils.getPotion(itemstack).getEffects().isEmpty())
            return;

        MutableComponent desc = Component.translatable("item.sortilege.antidote.cures").withStyle(ChatFormatting.DARK_PURPLE)
                .append(" ");

        MobEffectInstance effect = PotionUtils.getPotion(itemstack).getEffects().get(0);
        if (effect.getEffect().isBeneficial())
            desc.append(Component.translatable(effect.getDescriptionId()).withStyle(ChatFormatting.BLUE));
        else
            desc.append(Component.translatable(effect.getDescriptionId()).withStyle(ChatFormatting.RED));

        list.add(desc);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        Player player = entity instanceof Player ? (Player) entity : null;
        if (player instanceof ServerPlayer)
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer) player, stack);

        if (!world.isClientSide()) {
            MobEffect effect = PotionUtils.getMobEffects(stack).get(0).getEffect();

            if (entity.hasEffect(effect)) {
                entity.removeEffect(effect);
                int color = PotionUtils.getColor(stack);
                float r = FastColor.ARGB32.red(color) / 255f,
                      g = FastColor.ARGB32.green(color) / 255f,
                      b = FastColor.ARGB32.blue(color) / 255f;
                ModParticles.spawnWisps(world, entity.getX(), entity.getEyeY(), entity.getZ(), 16, new float[]{r, g, b});
            }
            if (ModConfig.antidoteImmunityTime.get() > 0)
                ((PotionShenanigans) entity).sorti_setImmunity(effect, ModConfig.antidoteImmunityTime.get() * 20);
        }

        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild)
                stack.shrink(1);
        }

        if (player == null)
            return stack;
        if (!player.getAbilities().instabuild) {
            if (stack.isEmpty())
                return new ItemStack(Items.GLASS_BOTTLE);

            player.getInventory().placeItemBackInInventory(new ItemStack(Items.GLASS_BOTTLE));
        }

        entity.gameEvent(GameEvent.DRINK);
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!PotionUtils.getPotion(itemstack).getEffects().isEmpty() && (player.isShiftKeyDown() ||
                player.hasEffect(PotionUtils.getMobEffects(itemstack).get(0).getEffect())))

            return ItemUtils.startUsingInstantly(level, player, hand);

        return InteractionResultHolder.fail(itemstack);
    }

    @Override
    public int getUseDuration(ItemStack itemstack) {
        return super.getUseDuration(itemstack) / 2;
    }

    public static int getItemColor(ItemStack itemstack, int i) {
        if (i == 0)
            return PotionUtils.getColor(itemstack);
        return -1;
    }
}
