package net.lyof.sortilege.item.custom.staff;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.lcc.sollib.api.client.render.MockItemRenderer;
import net.lcc.sollib.api.client.render.item.IAddedBarItem;
import net.lcc.sollib.api.client.render.item.IAddedRenderItem;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.attribute.ModAttributes;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.enchant.staff.ElementalStaffEnchantment;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.EnchantHelper;
import net.lyof.sortilege.util.MathHelper;
import net.lyof.sortilege.util.XPHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class StaffItem extends TieredItem implements DyeableLeatherItem, IAddedRenderItem, IAddedBarItem {
    private static final float[] COLOR_NONE = new float[]{1f, 1f, 1f};

    protected float damage;
    protected int pierce;
    protected int range;
    public int cooldown;
    public int charge;
    public int xp_cost;

    public @Nullable InteractionHand handSave;


    /*public StaffItem(ModConfigS.StaffInfo stats, FabricItemSettings settings) {
        this(stats.tier, stats.damage, stats.pierce, stats.range, stats.durability, stats.cooldown, stats.charge_time, stats.xp_cost,
                stats.fireRes ? settings.fireproof() : settings);
        this.rawInfos = stats;
    }*/

    public StaffItem(Tier tier, int damage, int targets, int range, int dura, int cooldown, int charge, int xp_cost,
                     FabricItemSettings settings) {
        super(tier, settings.durability(dura));

        this.damage = damage;
        this.pierce = targets;
        this.range = range;
        this.cooldown = cooldown;
        this.charge = charge;
        this.xp_cost = xp_cost;

        CauldronInteraction.WATER.put(this, CauldronInteraction.DYED_ITEM);
    }


    public int getXPCost(ItemStack itemstack) {
        return Math.max(this.xp_cost + EnchantHelper.getEnchantLevel(ModEnchants.IGNORANCE_CURSE, itemstack)
                - EnchantHelper.getEnchantLevel(ModEnchants.WISDOM, itemstack), 0);
    }

    public float getAttackDamage(ItemStack stack) {
        return this.damage + EnchantHelper.getEnchantLevel(ModEnchants.POTENCY, stack);
    }

    public int getAttackRange(ItemStack stack) {
        return this.range + EnchantHelper.getEnchantLevel(ModEnchants.STABILITY, stack)*2;
    }

    public int getPierce(ItemStack stack) {
        return this.pierce + EnchantHelper.getEnchantLevel(ModEnchants.CHAINING, stack);
    }

    private int getCooldown(ItemStack stack, Player player) {
        float multiplier = 1 - EnchantHelper.getEnchantLevel(ModEnchants.FOCUS, stack) * 0.05f;

        if (stack.is(ModTags.Items.XP_BOOSTED))
            multiplier -= player.experienceLevel / 200f;

        multiplier = Math.max(multiplier, 0);
        return (int) (this.cooldown * multiplier);
    }

    public List<float[]> getBeamColors(ItemStack stack, Set<ElementalStaffEnchantment> elements) {
        List<float[]> result = new ArrayList<>();

        int rgb = this.getColor(stack);
        float[] color = new float[]{FastColor.ARGB32.red(rgb) / 255f,
                FastColor.ARGB32.green(rgb) / 255f,
                FastColor.ARGB32.blue(rgb) / 255f};

        if (rgb == DyeableLeatherItem.DEFAULT_LEATHER_COLOR) {
            /*if (this.rawInfos != null && !this.rawInfos.colors.isEmpty())
                return this.rawInfos.colors;*/

            for (ElementalStaffEnchantment element : elements)
                result.addAll(element.colors);
            if (result.isEmpty())
                result.add(COLOR_NONE);
            if (stack.isEnchanted())
                result.add(new float[]{0.7f, 0f, 1f});
        }
        else {
            for (int i = 0 ; i < 5; i++) {
                result.add(new float[]{(float) (color[0] + Math.random()*0.1 - 0.05),
                        (float) (color[1] + Math.random()*0.1 - 0.05),
                        (float) (color[2] + Math.random()*0.1 - 0.05)});
            }
        }

        return result;
    }

    public Set<ElementalStaffEnchantment> getElements(ItemStack stack) {
        Set<ElementalStaffEnchantment> elements = new HashSet<>();
        for (Enchantment enchant : EnchantmentHelper.getEnchantments(stack).keySet()) {
            if (enchant instanceof ElementalStaffEnchantment element)
                elements.add(element);
        }
        return elements;
    }

    private static final String OVERCHARGE_NBT = Sortilege.MOD_ID +  "Overcharge";

    public int getOvercharge(ItemStack stack) {
        if (!stack.hasTag()) return 0;
        return stack.getOrCreateTag().getInt(OVERCHARGE_NBT);
    }

    public void setOvercharge(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(OVERCHARGE_NBT, Math.min(value, this.getMaxOvercharge(stack)));
    }

    public int getMaxOvercharge(ItemStack stack) {
        return 0;
    }


    @Override
    public boolean isValidRepairItem(ItemStack staff, ItemStack stack) {
        /*if (this.rawInfos != null)
            return this.rawInfos.repair.get().test(stack);
        */return super.isValidRepairItem(staff, stack);
    }

    @Override
    public int getEnchantmentValue() {
        /*if (this.rawInfos != null) return this.rawInfos.enchantability;
        */return super.getEnchantmentValue();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND && this.pierce > 0) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();

            if (EnchantHelper.hasEnchant(ModEnchants.BONK, stack)) {
                builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,
                        "Weapon modifier", this.getAttackDamage(stack)-1, AttributeModifier.Operation.ADDITION));
                builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID,
                        "Weapon modifier", -3, AttributeModifier.Operation.ADDITION));
            }

            builder.put(ModAttributes.STAFF_DAMAGE, new AttributeModifier(ModAttributes.STAFF_DAMAGE.getUUID(),
                    "Weapon modifier", this.getAttackDamage(stack), AttributeModifier.Operation.ADDITION));
            builder.put(ModAttributes.STAFF_PIERCE, new AttributeModifier(ModAttributes.STAFF_PIERCE.getUUID(),
                    "Weapon modifier", this.getPierce(stack), AttributeModifier.Operation.ADDITION));
            builder.put(ModAttributes.STAFF_RANGE, new AttributeModifier(ModAttributes.STAFF_RANGE.getUUID(),
                    "Weapon modifier", this.getAttackRange(stack), AttributeModifier.Operation.ADDITION));

            return builder.build();
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);

        // Undergarden compat
        if (stack.is(ModTags.Items.FROSTSTEEL_ITEMS))
            tooltip.add(Component.translatable("tooltip.froststeel_sword").withStyle(ChatFormatting.AQUA));
        if (stack.is(ModTags.Items.UTHERIUM_ITEMS))
            tooltip.add(Component.translatable("tooltip.utheric_sword").withStyle(ChatFormatting.RED));
        if (stack.is(ModTags.Items.FORGOTTEN_ITEMS))
            tooltip.add(Component.translatable("tooltip.forgotten_sword").withStyle(ChatFormatting.GREEN));

        if (world != null && world.isClientSide())
            tooltip.add(Component.translatable("sortilege.staff.cooldown", this.getCooldown(stack, Minecraft.getInstance().player) / 20f)
                    .withStyle(ChatFormatting.GRAY));
        if (this.getXPCost(stack) > 0) {
            tooltip.add(Component.translatable("sortilege.staff.experience_cost", this.getXPCost(stack))
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.empty());
        }
    }


    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference) {
        if (clickType != ClickAction.SECONDARY) return false;

        String id = BuiltInRegistries.ITEM.getKey(other.getItem()).toString();
        /*if (ModConfig.overchargeIngredients.containsKey(id) && this.getOvercharge(stack) < this.getMaxOvercharge(stack)) {
            other.shrink(1);
            this.setOvercharge(stack, this.getOvercharge(stack) + ModConfig.overchargeIngredients.get(id).intValue());

            return true;
        }*/
        return false;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack staff = player.getItemInHand(hand);
        if (!player.isCreative() && !XPHelper.hasXP(player, this.getXPCost(staff)) && this.getOvercharge(staff) <= 0)
            return super.use(world, player, hand);
        if (player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND).getItem() instanceof ShieldItem
                && player.isShiftKeyDown())
            return super.use(world, player, hand);

        this.handSave = hand;
        player.startUsingItem(hand);
        return super.use(world, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack staff, Level world, LivingEntity entity) {
        if (!(entity instanceof Player player))
            return staff;

        Set<ElementalStaffEnchantment> elements = this.getElements(staff);

        int cost = this.getXPCost(staff);
        float damage = this.getAttackDamage(staff);
        int range = this.getAttackRange(staff);
        int targetsLeft = this.getPierce(staff);


        if (cost > 0 && !player.isCreative() && !(this.getOvercharge(staff) > 0/* && ModConfig.overchargePreventsExperience*/)) {
            if (!XPHelper.hasXP(player, cost))
                return staff;
            player.giveExperiencePoints(-cost);
        }

        if (this.handSave != null)
            player.swing(this.handSave, true);

        world.playSound(player, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1, 1);
        player.getCooldowns().addCooldown(staff.getItem(), this.getCooldown(staff, player));

        if (this.getOvercharge(staff) <= 0/* || !ModConfig.overchargePreventsDurability*/)
            staff.hurtAndBreak(1, player, e -> e.broadcastBreakEvent(this.handSave));
        if (this.getOvercharge(staff) > 0)
            this.setOvercharge(staff, this.getOvercharge(staff) - 1);


        // Getting the look vector to shoot the ray along
        Vec3 look = MathHelper.getLookVector(player);

        // Initialising variables to be used in the loop
        List<String> targetsHit = new ArrayList<>();
        int index;

        float x = (float) player.getX();
        float y = (float) player.getY();
        float z = (float) player.getZ();
        BlockPos pos;


        /*if (world instanceof ServerLevel server && this.rawInfos != null) {
            server.getServer().getCommands().performPrefixedCommand(
                    player.createCommandSourceStack().withMaximumPermission(Commands.LEVEL_OWNERS),
                    this.rawInfos.on_shoot);
        }*/

        List<float[]> colors = this.getBeamColors(staff, elements);

        int step = 5;
        // Main loop, displaying particles and hurting mobs on its way
        for (int i = 1; i < range * step; i++) {
            x = (float) (player.getX() + look.x * i/step);
            y = (float) (player.getY() + look.y * i/step + player.getEyeHeight(player.getPose()) - 0.2);
            z = (float) (player.getZ() + look.z * i/step);

            ModParticles.spawnWisps(world, x, y, z, 1, MathHelper.randi(colors));

            if (i % step != 0)
                continue;

            pos = new BlockPos((int) Math.round(x-0.5), (int) Math.round(y-0.5), (int) Math.round(z-0.5));
            Vec3 vec = new Vec3(x, y, z).subtract(pos.getX(), pos.getY(), pos.getZ());
            List<Entity> entities = player.level().getEntities(player, new AABB(pos).inflate(0.1));

            if (world.getBlockState(pos).getCollisionShape(world, pos).toAabbs()
                    .stream().anyMatch(box -> box.contains(vec))) {
                /*if (ModConfig.staffsPierceBlocks)
                    targetsLeft--;
                else*/
                    break;
            }
            if (targetsLeft <= 0)
                break;

            index = 0;
            while (!entities.isEmpty() && entities.size() > index && targetsLeft > 0) {

                if (entities.get(index) instanceof LivingEntity target
                        && !targetsHit.contains(target.getStringUUID()) && StaffItem.canHit(player, target)) {

                    this.triggerAttack(target, player, staff, elements, look, true, damage, targetsHit);

                    //targetsHit.add(target.getUuidAsString());
                    targetsLeft--;
                }
                index++;
            }
        }
        if (elements.contains((ElementalStaffEnchantment) ModEnchants.BLAST))
            this.triggerBlastAttack(player, staff, elements, look, damage, x, y, z, 2, targetsHit);
            //world.createExplosion(player, x, y, z, 1, World.ExplosionSourceType.NONE);

        return staff;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (EnchantHelper.hasEnchant(ModEnchants.BONK, stack)) {
            this.triggerAttack(target, attacker, stack, this.getElements(stack), MathHelper.getLookVector(attacker), true,
                    this.getAttackDamage(stack), new ArrayList<>());
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    public void triggerAttack(LivingEntity target, LivingEntity attacker, ItemStack stack, Set<ElementalStaffEnchantment> elements,
                              Vec3 direction, boolean propagate, float damage, List<String> targetsHit) {

        if (targetsHit.contains(target.getStringUUID())) return;

        Level world = attacker.level();
        float kinesis = EnchantHelper.getEnchantLevel(ModEnchants.PUSH, stack) - EnchantHelper.getEnchantLevel(ModEnchants.PULL, stack);
        float d = this.modifyDamageDealt(damage, stack, target, elements);

        if (d > 0)
            target.hurt(attacker.damageSources().indirectMagic(attacker, attacker), d);
        else if (d < 0) {
            target.heal(-d);
            ModParticles.spawnWisps(world, target.getX(), target.getY() + target.getEyeHeight() / 2, target.getZ(),
                    10, new float[]{1, 0.5f, 0.5f});
        }

        targetsHit.add(target.getStringUUID());

        /*if (world instanceof ServerLevel server && this.rawInfos != null) {
            server.getServer().getCommands().performPrefixedCommand(
                    attacker.createCommandSourceStack().withMaximumPermission(4),
                    this.rawInfos.on_hit_self);

            server.getServer().getCommands().performPrefixedCommand(
                    target.createCommandSourceStack().withMaximumPermission(4),
                    this.rawInfos.on_hit_target);
        }*/

        if (kinesis != 0)
            target.setDeltaMovement(direction.add(0, 0.07, 0).normalize().scale(kinesis * 0.55));

        for (ElementalStaffEnchantment element : elements) {
            int elementLevel = EnchantHelper.getEnchantLevel(element, stack);

            element.triggerAttack(target, elementLevel);
            if (element == ModEnchants.BLAST && elementLevel > 1 && propagate) {
                this.triggerBlastAttack(attacker, stack, elements, direction, damage,
                        target.getX(), target.getY() + target.getEyeHeight() / 2, target.getZ(),
                        2, targetsHit);
            }
        }
    }

    public void triggerBlastAttack(LivingEntity attacker, ItemStack stack, Set<ElementalStaffEnchantment> elements, Vec3 direction, float damage,
                                   double x, double y, double z, double radius, List<String> targetsHit) {

        if (attacker.level().isClientSide())
            attacker.level().explode(attacker, x, y, z, 1, Level.ExplosionInteraction.NONE);

        Vec3 pos = new Vec3(x, y, z);
        Vec3 offset = new Vec3(radius, radius, radius);

        for (Entity entity : attacker.level().getEntities(attacker, new AABB(pos.subtract(offset), pos.add(offset)))) {
            if (entity instanceof LivingEntity target && StaffItem.canHit(attacker, target)) {
                this.triggerAttack(target, attacker, stack, elements, direction, false, damage, targetsHit);
            }
        }
    }

    public static boolean canHit(LivingEntity shooter, LivingEntity target) {
        return !(target instanceof OwnableEntity tameable && tameable.getOwner() == shooter) && !target.getPassengers().contains(shooter);
    }

    public float modifyDamageDealt(float damage, ItemStack stack, LivingEntity target, Set<ElementalStaffEnchantment> elements) {
        if (elements.contains((ElementalStaffEnchantment) ModEnchants.BLESSING)) {
            if (target.getType().is(ModTags.Entities.UNDEAD))
                damage *= 1 + EnchantHelper.getEnchantLevel(ModEnchants.BLESSING, stack) * 0.5f;
            else if (!ModConfig.altBlessing.get() || !(target instanceof Enemy))
                damage *= EnchantHelper.getEnchantLevel(ModEnchants.BLESSING, stack) * -0.75f;
        }

        // Undergarden compat
        if (target.getType().is(ModTags.Entities.UNDERGARDEN_ENTITIES) && stack.is(ModTags.Items.FORGOTTEN_ITEMS))
            damage *= 1.5f;
        if (target.getType().is(ModTags.Entities.ROTSPAWN) && stack.is(ModTags.Items.UTHERIUM_ITEMS))
            damage *= 1.5f;

        return damage;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return this.charge;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }


    private static final ResourceLocation COLOR_OVERLAY = Sortilege.MOD.makeID("textures/models/staff/glint.png");

    @Override
    public boolean shouldAddRender(ItemStack stack) {
        return this.hasCustomColor(stack) && !stack.is(ModTags.Items.NO_DYE_OVERLAY_STAFFS);
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext context, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        matrices.scale(1.005f, 1.005f, 1.005f);
        matrices.translate(0, 0.995, 0.4975);
        matrices.mulPose(Axis.XP.rotationDegrees(180));

        MockItemRenderer.renderTintedItem(matrices, vertexConsumers, light, COLOR_OVERLAY, this.getColor(stack));
    }

    @Override
    public boolean shouldAddBarRender(ItemStack stack) {
        return this.getOvercharge(stack) > 0;
    }

    @Override
    public float getAddedBarFullness(ItemStack stack) {
        return this.getOvercharge(stack)  / (float) this.getMaxOvercharge(stack);
    }

    @Override
    public int getAddedBarColor(ItemStack itemStack) {
        return 0x0000ff;
    }
}