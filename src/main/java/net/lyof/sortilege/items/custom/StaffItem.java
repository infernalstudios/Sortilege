package net.lyof.sortilege.items.custom;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.ModJsonConfigs;
import net.lyof.sortilege.enchants.ModEnchants;
import net.lyof.sortilege.enchants.staff.ElementalStaffEnchantment;
import net.lyof.sortilege.particles.ModParticles;
import net.lyof.sortilege.utils.ItemHelper;
import net.lyof.sortilege.utils.MathHelper;
import net.lyof.sortilege.utils.XPHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.antlr.v4.runtime.misc.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StaffItem extends TieredItem {
    public @Nullable ModJsonConfigs.StaffInfo rawInfos;
    public float damage;
    public int pierce;
    public int range;
    public int cooldown;
    public int charge;
    public int xp_cost;

    public @Nullable InteractionHand handSave;


    public StaffItem(ModJsonConfigs.StaffInfo stats) {
        this(stats.tier, stats.damage, stats.pierce, stats.range, stats.durability, stats.cooldown, stats.charge_time, stats.xp_cost,
                stats.fireRes ?
                    new Item.Properties().tab(CreativeModeTab.TAB_COMBAT).fireResistant() :
                    new Item.Properties().tab(CreativeModeTab.TAB_COMBAT));
        this.rawInfos = stats;
    }

    public StaffItem(Tier pTier, int damage, int targets, int range, int dura, int cooldown, int charge, int xp_cost,
                     Properties properties) {
        super(pTier, properties.defaultDurability(dura));

        this.damage = damage;
        this.pierce = targets;
        this.range = range;
        this.cooldown = cooldown;
        this.charge = charge;
        this.xp_cost = xp_cost;
    }

    public int getXPCost(ItemStack itemstack) {
        return Math.max(this.xp_cost + ItemHelper.getEnchantLevel(ModEnchants.IGNORANCE_CURSE, itemstack)
                - ItemHelper.getEnchantLevel(ModEnchants.WISDOM, itemstack), 0);
    }

    public float getAttackDamage(ItemStack stack) {
        return this.damage + ItemHelper.getEnchantLevel(ModEnchants.POTENCY, stack);
    }

    public int getAttackRange(ItemStack stack) {
        return this.range + ItemHelper.getEnchantLevel(ModEnchants.STABILITY, stack)*2;
    }

    public int getPierce(ItemStack stack) {
        return this.pierce + ItemHelper.getEnchantLevel(ModEnchants.CHAINING, stack);
    }

    @Override
    public boolean isValidRepairItem(ItemStack staff, ItemStack stack) {
        if (Registry.ITEM.getKey(stack.getItem()).toString().equals("phantasm:oblivion"))
            return true;
        if (this.rawInfos != null)
            return this.rawInfos.repair.get().test(stack);
        return super.isValidRepairItem(staff, stack);
    }

    @Override
    public int getEnchantmentValue() {
        if (this.rawInfos != null) return this.rawInfos.enchantability;
        return super.getEnchantmentValue();
    }

    @Override
    public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemstack, level, list, flag);

        if (this.getXPCost(itemstack) > 0) {
            list.add(Component.translatable("sortilege.staff.cost")
                    .append(" " + this.getXPCost(itemstack) + " ")
                    .append(Component.translatable("sortilege.experience")).withStyle(ChatFormatting.GREEN));
            list.add(Component.literal(""));
        }

        //list.add(Component.literal("" + this.rawInfos));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack staff = player.getItemInHand(hand);
        if (!player.isCreative() && !XPHelper.hasXP(player, this.getXPCost(staff)))
            return super.use(world, player, hand);

        this.handSave = hand;
        player.startUsingItem(hand);
        return super.use(world, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack staff, Level world, LivingEntity entity) {
        if (!(entity instanceof Player player))
            return staff;

        ElementalStaffEnchantment element = null;
        if (ItemHelper.hasEnchant(ModEnchants.BRAZIER, staff))
            element = (ElementalStaffEnchantment) ModEnchants.BRAZIER.get();
        else if (ItemHelper.hasEnchant(ModEnchants.BLIZZARD, staff))
            element = (ElementalStaffEnchantment) ModEnchants.BLIZZARD.get();
        else if (ItemHelper.hasEnchant(ModEnchants.BLAST, staff))
            element = (ElementalStaffEnchantment) ModEnchants.BLAST.get();
        else if (ItemHelper.hasEnchant(ModEnchants.BLITZ, staff))
            element = (ElementalStaffEnchantment) ModEnchants.BLITZ.get();
        int element_level = element == null ? 0 : ItemHelper.getEnchantLevel(element, staff);


        int cost = this.getXPCost(staff);
        float damage = this.getAttackDamage(staff);
        int range = this.getAttackRange(staff);
        int targetsLeft = this.getPierce(staff);
        float kinesis = ItemHelper.getEnchantLevel(ModEnchants.PUSH, staff) - ItemHelper.getEnchantLevel(ModEnchants.PULL, staff);


        if (cost > 0 && !player.isCreative()) {
            if (!XPHelper.hasXP(player, cost))
                return staff;
            player.giveExperiencePoints(-cost);
        }

        if (this.handSave != null)
            player.swing(this.handSave, true);

        world.playSound(player, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1, 1);
        player.getCooldowns().addCooldown(staff.getItem(), this.cooldown);
        staff.hurtAndBreak(1, player, e -> e.broadcastBreakEvent(this.handSave));


        // Getting the look vector to shoot the ray along
        Vec3 look = MathHelper.getLookVector(player);

        // Initialising variables to be used in the loop
        List<String> targetsHit = new ArrayList<>();
        int index;

        float x = (float) player.getX();
        float y = (float) player.getY();
        float z = (float) player.getZ();
        BlockPos pos;


        if (world instanceof ServerLevel server && !(this.rawInfos == null)) {
            server.getServer().getCommands().performPrefixedCommand(
                    new CommandSourceStack(player, new Vec3(x, y, z), Vec2.ZERO, server,
                            4, "", Component.literal(""), server.getServer(), player)
                            .withSuppressedOutput(), this.rawInfos.on_shoot);
        }


        DamageSource damagetype = DamageSource.indirectMagic(player, player);
        if (element == ModEnchants.BRAZIER.get())
            damagetype.setIsFire();

        List<Triple<Float, Float, Float>> colors = new ArrayList<>(element == null ? List.of(new Triple<>(1f, 1f, 1f)) : element.colors);
        if (staff.isEnchanted())
            colors.add(new Triple<>(0.7f, 0f, 1f));
        if (this.rawInfos != null && this.rawInfos.colors.size() > 0)
            colors = this.rawInfos.colors;


        int step = 5;
        // Main loop, displaying particles and hurting mobs on its way
        for (int i = 1; i < range * step; i++) {
            x = (float) (player.getX() + look.x * i/step);
            y = (float) (player.getY() + look.y * i/step + player.getEyeHeight() - 0.5);
            z = (float) (player.getZ() + look.z * i/step);

            if (world.isClientSide())
                ModParticles.spawnWisps(world, x, y, z, 1, MathHelper.randi(colors));
                //WispParticle.COLOR = MathHelper.randi(colors);
                //serverworld.sendParticles(particle, x, y, z, 1, 0, 0, 0, 0);

            if (i*2 % step != 0)
                continue;

            pos = new BlockPos(Math.round(x-0.5), Math.round(y-0.5), Math.round(z-0.5));
            List<Entity> entities = player.getLevel().getEntities(null, new AABB(pos).inflate(0.1));

            if (targetsLeft <= 0 || player.level.getBlockState(pos).canOcclude())
                break;

            index = 0;
            while (!entities.isEmpty() && entities.size() > index
                    && !entities.get(index).equals(player) && targetsLeft > 0) {

                if (entities.get(index) instanceof LivingEntity target
                        && !targetsHit.contains(target.getStringUUID())) {

                    target.hurt(damagetype, damage);
                    if (world instanceof ServerLevel server && !(this.rawInfos == null)) {
                        server.getServer().getCommands().performPrefixedCommand(
                                new CommandSourceStack(player, new Vec3(x, y, z), Vec2.ZERO, server,
                                        4, "", Component.literal(""), server.getServer(), player)
                                        .withSuppressedOutput(), this.rawInfos.on_hit_self);

                        server.getServer().getCommands().performPrefixedCommand(
                                new CommandSourceStack(player, new Vec3(x, y, z), Vec2.ZERO, server,
                                        4, "", Component.literal(""), server.getServer(), target)
                                        .withSuppressedOutput(), this.rawInfos.on_hit_target);
                    }


                    if (element != null)
                        element.triggerAttack(target, ItemHelper.getEnchantLevel(element, staff));
                    if (element == ModEnchants.BLAST.get() && element_level > 1)
                        world.explode(player, x, y, z, 1, Explosion.BlockInteraction.NONE);

                    if (kinesis != 0)
                        target.setDeltaMovement(look.scale(kinesis).add(0, 0.4, 0));

                    targetsHit.add(target.getStringUUID());
                    targetsLeft--;
                }
                index++;
            }
        }
        if (element == ModEnchants.BLAST.get())
            world.explode(player, x, y, z, 1, Explosion.BlockInteraction.NONE);

        return staff;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return this.charge;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }
}