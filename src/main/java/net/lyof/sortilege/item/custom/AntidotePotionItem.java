package net.lyof.sortilege.item.custom;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.lyof.sortilege.item.potion.PotionShenanigans;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.List;

public class AntidotePotionItem extends PotionItem {
    public AntidotePotionItem(Properties settings) {
        super(settings);
    }

    public static void fillItemGroup(FabricItemGroupEntries entries, Item antidote) {
        if (!ModConfig.antidoteEnabled.get()) return;

        for (Holder<Potion> potion : PotionHelper.POTIONS.values())
            entries.accept(PotionContents.createItemStack(antidote, potion));
    }

    @Override
    public Component getName(ItemStack stack) {
        if (PotionHelper.getEffects(stack).isEmpty())
            return super.getName(stack);

        return Component.translatable(PotionHelper.getEffects(stack).get(0).getDescriptionId())
                .append(" ")
                .append(Component.translatable(this.getDescriptionId()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
        if (PotionHelper.getEffects(stack).isEmpty())
            return;

        MutableComponent desc = Component.translatable("tooltip.sortilege.antidote.cures").withStyle(ChatFormatting.DARK_PURPLE)
                .append(" ");

        MobEffectInstance effect = PotionHelper.getEffects(stack).get(0);
        if (effect.getEffect().value().isBeneficial())
            desc.append(Component.translatable(effect.getDescriptionId()).withStyle(ChatFormatting.BLUE));
        else
            desc.append(Component.translatable(effect.getDescriptionId()).withStyle(ChatFormatting.RED));

        list.add(desc);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (!stack.has(DataComponents.POTION_CONTENTS)) return stack;

        Player player = entity instanceof Player ? (Player) entity : null;
        if (player instanceof ServerPlayer)
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer) player, stack);

        if (!world.isClientSide()) {
            Holder<MobEffect> effect = PotionHelper.getEffects(stack).get(0).getEffect();

            if (entity.hasEffect(effect)) {
                entity.removeEffect(effect);
                ModParticles.sendParticles(world, entity.getX(), entity.getEyeY(), entity.getZ(), 16,
                        stack.get(DataComponents.POTION_CONTENTS).getColor());
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
        ItemStack stack = player.getItemInHand(hand);

        if (!PotionHelper.getEffects(stack).isEmpty() && (player.isShiftKeyDown() ||
                player.hasEffect(PotionHelper.getEffects(stack).get(0).getEffect())))

            return ItemUtils.startUsingInstantly(level, player, hand);

        return InteractionResultHolder.fail(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return super.getUseDuration(stack, user) / 2;
    }

    public static int getItemColor(ItemStack stack, int i) {
        if (i == 0 && stack.has(DataComponents.POTION_CONTENTS))
            return stack.get(DataComponents.POTION_CONTENTS).getColor();
        return -1;
    }
}
