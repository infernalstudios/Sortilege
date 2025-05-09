package net.lyof.sortilege.item.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.item.v1.ModifyItemAttributeModifiersCallback;
import net.lyof.sortilege.attribute.ModAttributes;
import net.lyof.sortilege.config.ModConfig;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.enchant.staff.ElementalStaffEnchantment;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.ItemHelper;
import net.lyof.sortilege.util.MathHelper;
import net.lyof.sortilege.util.XPHelper;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StaffItem extends ToolItem implements DyeableItem {
    private static final float[] COLOR_NONE = new float[]{1f, 1f, 1f};

    public @Nullable ModConfig.StaffInfo rawInfos;
    public float damage;
    public int pierce;
    public int range;
    public int cooldown;
    public int charge;
    public int xp_cost;

    public @Nullable Hand handSave;


    public StaffItem(ModConfig.StaffInfo stats, FabricItemSettings settings) {
        this(stats.tier, stats.damage, stats.pierce, stats.range, stats.durability, stats.cooldown, stats.charge_time, stats.xp_cost,
                stats.fireRes ? settings.fireproof() : settings);
        this.rawInfos = stats;
    }

    public StaffItem(ToolMaterial tier, int damage, int targets, int range, int dura, int cooldown, int charge, int xp_cost,
                     FabricItemSettings settings) {
        super(tier, settings.maxDamage(dura));

        this.damage = damage;
        this.pierce = targets;
        this.range = range;
        this.cooldown = cooldown;
        this.charge = charge;
        this.xp_cost = xp_cost;

        CauldronBehavior.WATER_CAULDRON_BEHAVIOR.put(this, CauldronBehavior.CLEAN_DYEABLE_ITEM);
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

    private int getCooldown(ItemStack stack, PlayerEntity player) {
        float multiplier = 1;
        if (stack.isIn(ModTags.Items.XP_BOOSTED))
            multiplier -= player.experienceLevel / 200f;
        return (int) (this.cooldown * multiplier);
    }

    public List<float[]> getBeamColors(ItemStack stack, @Nullable ElementalStaffEnchantment element) {
        List<float[]> result = new ArrayList<>();

        int rgb = this.getColor(stack);
        float[] color = new float[]{ColorHelper.Argb.getRed(rgb) / 255f,
                ColorHelper.Argb.getGreen(rgb) / 255f,
                ColorHelper.Argb.getBlue(rgb) / 255f};
        if (rgb == 10511680) {
            if (element != null)
                result.addAll(element.colors);
            if (stack.hasEnchantments())
                result.add(new float[]{0.7f, 0f, 1f});
            if (this.rawInfos != null && !this.rawInfos.colors.isEmpty())
                result.addAll(this.rawInfos.colors);
        }
        else {
            for (int i = 0 ; i < 5; i++) {
                result.add(new float[]{(float) (color[0] + Math.random()*0.1 - 0.05),
                        (float) (color[1] + Math.random()*0.1 - 0.05),
                        (float) (color[2] + Math.random()*0.1 - 0.05)});
            }
        }

        if (result.isEmpty())
            result.add(COLOR_NONE);

        return result;
    }

    public static int getOvercharge(ItemStack stack) {
        return 3;
    }

    public static int getMaxOvercharge(ItemStack stack) {
        return 8;
    }


    @Override
    public boolean canRepair(ItemStack staff, ItemStack stack) {
        if (this.rawInfos != null)
            return this.rawInfos.repair.get().test(stack);
        return super.canRepair(staff, stack);
    }

    @Override
    public int getEnchantability() {
        if (this.rawInfos != null) return this.rawInfos.enchantability;
        return super.getEnchantability();
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
            builder.put(ModAttributes.STAFF_DAMAGE, new EntityAttributeModifier(ModAttributes.STAFF_DAMAGE.getUUID(), "Weapon modifier", this.getAttackDamage(stack), EntityAttributeModifier.Operation.ADDITION));
            builder.put(ModAttributes.STAFF_PIERCE, new EntityAttributeModifier(ModAttributes.STAFF_PIERCE.getUUID(), "Weapon modifier", this.getPierce(stack), EntityAttributeModifier.Operation.ADDITION));
            builder.put(ModAttributes.STAFF_RANGE, new EntityAttributeModifier(ModAttributes.STAFF_RANGE.getUUID(), "Weapon modifier", this.getAttackRange(stack), EntityAttributeModifier.Operation.ADDITION));
            return builder.build();
        }
        return super.getAttributeModifiers(slot);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> list, TooltipContext flag) {
        super.appendTooltip(stack, world, list, flag);

        if (world != null && world.isClient())
            list.add(Text.translatable("sortilege.staff.cooldown", this.getCooldown(stack, MinecraftClient.getInstance().player) / 20f)
                    .formatted(Formatting.GRAY));
        if (this.getXPCost(stack) > 0) {
            list.add(Text.translatable("sortilege.staff.experience_cost", this.getXPCost(stack))
                    .formatted(Formatting.GREEN));
            list.add(Text.empty());
        }
    }

    public int getOverchargeBarColor(ItemStack stack) {
        return 255;
    }

    public int getOverchargeBarStep(ItemStack stack) {
        return Math.round(13.0F - (float) getOvercharge(stack) * 13.0F / (float) getMaxOvercharge(stack));
    }

    @Override
    public @NotNull TypedActionResult<ItemStack> use(@NotNull World world, PlayerEntity player, @NotNull Hand hand) {
        ItemStack staff = player.getStackInHand(hand);
        if (!player.isCreative() && !XPHelper.hasXP(player, this.getXPCost(staff)))
            return super.use(world, player, hand);

        this.handSave = hand;
        player.setCurrentHand(hand);
        return super.use(world, player, hand);
    }

    @Override
    public ItemStack finishUsing(ItemStack staff, World world, LivingEntity entity) {
        if (!(entity instanceof PlayerEntity player))
            return staff;

        ElementalStaffEnchantment element = null;
        if (ItemHelper.hasEnchant(ModEnchants.BRAZIER, staff))
            element = (ElementalStaffEnchantment) ModEnchants.BRAZIER;
        else if (ItemHelper.hasEnchant(ModEnchants.BLIZZARD, staff))
            element = (ElementalStaffEnchantment) ModEnchants.BLIZZARD;
        else if (ItemHelper.hasEnchant(ModEnchants.BLAST, staff))
            element = (ElementalStaffEnchantment) ModEnchants.BLAST;
        else if (ItemHelper.hasEnchant(ModEnchants.BLITZ, staff))
            element = (ElementalStaffEnchantment) ModEnchants.BLITZ;
        int element_level = ItemHelper.getEnchantLevel(element, staff);


        int cost = this.getXPCost(staff);
        float damage = this.getAttackDamage(staff);
        int range = this.getAttackRange(staff);
        int targetsLeft = this.getPierce(staff);
        float kinesis = ItemHelper.getEnchantLevel(ModEnchants.PUSH, staff) - ItemHelper.getEnchantLevel(ModEnchants.PULL, staff);


        if (cost > 0 && !player.isCreative()) {
            if (!XPHelper.hasXP(player, cost))
                return staff;
            player.addExperience(-cost);
        }

        if (this.handSave != null)
            player.swingHand(this.handSave, true);

        world.playSound(player, player.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, SoundCategory.PLAYERS, 1, 1);
        player.getItemCooldownManager().set(staff.getItem(), this.getCooldown(staff, player));
        staff.damage(1, player, e -> e.sendToolBreakStatus(this.handSave));


        // Getting the look vector to shoot the ray along
        Vec3d look = MathHelper.getLookVector(player);

        // Initialising variables to be used in the loop
        List<String> targetsHit = new ArrayList<>();
        int index;

        float x = (float) player.getX();
        float y = (float) player.getY();
        float z = (float) player.getZ();
        BlockPos pos;


        if (world instanceof ServerWorld server && !(this.rawInfos == null)) {
            server.getServer().getCommandManager().executeWithPrefix(
                    new ServerCommandSource(player, new Vec3d(x, y, z), Vec2f.ZERO, server,
                            4, "", Text.literal(""), server.getServer(), player)
                            .withOutput(CommandOutput.DUMMY),
                    this.rawInfos.on_shoot);
        }

        DamageSource damagetype = player.getDamageSources().indirectMagic(player, player);

        List<float[]> colors = this.getBeamColors(staff, element);

        int step = 5;
        // Main loop, displaying particles and hurting mobs on its way
        for (int i = 1; i < range * step; i++) {
            x = (float) (player.getX() + look.x * i/step);
            y = (float) (player.getY() + look.y * i/step + player.getEyeHeight(player.getPose()) - 0.2);
            z = (float) (player.getZ() + look.z * i/step);

            ModParticles.spawnWisps(world, x, y, z, 1, MathHelper.randi(colors));

            if (i*2 % step != 0)
                continue;

            pos = new BlockPos((int) Math.round(x-0.5), (int) Math.round(y-0.5), (int) Math.round(z-0.5));
            List<Entity> entities = player.getWorld().getOtherEntities(player, new Box(pos).expand(0.1));

            if (targetsLeft <= 0 || world.getBlockState(pos).isSolid())
                break;

            index = 0;
            while (!entities.isEmpty() && entities.size() > index
                    && targetsLeft > 0) {

                if (entities.get(index) instanceof LivingEntity target
                        && !targetsHit.contains(target.getUuidAsString())) {

                    target.damage(damagetype, damage);
                    if (world instanceof ServerWorld server && !(this.rawInfos == null)) {
                        server.getServer().getCommandManager().executeWithPrefix(
                                player.getCommandSource().withMaxLevel(4).withOutput(CommandOutput.DUMMY),
                                this.rawInfos.on_hit_self);

                        server.getServer().getCommandManager().executeWithPrefix(
                                target.getCommandSource().withMaxLevel(4).withOutput(CommandOutput.DUMMY),
                                this.rawInfos.on_hit_target);
                    }


                    if (element != null)
                        element.triggerAttack(target, ItemHelper.getEnchantLevel(element, staff));
                    if (element == ModEnchants.BLAST && element_level > 1)
                        world.createExplosion(player, x, y, z, 1, World.ExplosionSourceType.NONE);

                    if (kinesis != 0)
                        target.setVelocity(look.add(0, 0.07, 0).normalize().multiply(kinesis * 0.6));

                    targetsHit.add(target.getUuidAsString());
                    targetsLeft--;
                }
                index++;
            }
        }
        if (element == ModEnchants.BLAST)
            world.createExplosion(player, x, y, z, 1, World.ExplosionSourceType.NONE);

        return staff;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return this.charge;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }
}