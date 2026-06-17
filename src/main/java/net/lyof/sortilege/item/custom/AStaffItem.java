package net.lyof.sortilege.item.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lcc.sollib.api.client.render.MockItemRenderer;
import net.lcc.sollib.api.client.render.item.IAddedBarItem;
import net.lcc.sollib.api.client.render.item.IAddedRenderItem;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.attribute.ModAttributes;
import net.lyof.sortilege.enchant.IBuiltinEnchantsItem;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.enchant.staff.ElementalStaffEnchantment;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.EnchantHelper;
import net.lyof.sortilege.util.MathHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import org.jetbrains.annotations.Nullable;
import vazkii.botania.client.fx.BotaniaParticles;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.item.equipment.bauble.ManaseerMonocleItem;
import vazkii.botania.common.proxy.Proxy;
import vazkii.botania.xplat.BotaniaConfig;

import java.util.*;

public abstract class AStaffItem extends TieredItem implements DyeableLeatherItem, IAddedRenderItem, IAddedBarItem,
                                                               IBuiltinEnchantsItem {
    private static final float[] COLOR_NONE = new float[]{1f, 1f, 1f};
    private static final float[] COLOR_ENCHANTED = new float[]{0.7f, 0f, 1f};

    private static final ResourceLocation COLOR_OVERLAY = Sortilege.MOD.makeID("textures/models/staff/glint.png");
    private static final String OVERCHARGE_NBT = "sorti_Overcharge";

    protected final StaffEntry entry;
    protected InteractionHand hand;

    public AStaffItem(StaffEntry entry, Properties properties) {
        super(entry.getTier(), entry.getTier().isFireproof() ? properties.fireResistant() : properties);
        this.entry = entry;
    }

    public StaffEntry getEntry() {
        return this.entry;
    }

    //#region Rendering
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
        return (float) this.getOvercharge(stack) / this.getMaxOvercharge(stack);
    }

    @Override
    public int getAddedBarColor(ItemStack stack) {
        return this.getEntry().getCost().getOvercharge().getColor();
    }
    //#endregion


    //#region Display
    @Override
    public int getUseDuration(ItemStack stack) {
        return this.getEntry().getTier().getChargeTime();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);

        int i = tooltip.size();
        this.appendTooltipAbilities(stack, Minecraft.getInstance().player, tooltip);
        if (i < tooltip.size()) tooltip.add(Component.empty());

        i = tooltip.size();
        this.appendTooltipCosts(stack, Minecraft.getInstance().player, tooltip);
        if (i < tooltip.size()) tooltip.add(Component.empty());
    }
    //#endregion


    //#region Overcharge
    public int getOvercharge(ItemStack stack) {
        if (!stack.hasTag()) return 0;
        return stack.getOrCreateTag().getInt(OVERCHARGE_NBT);
    }

    public void setOvercharge(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(OVERCHARGE_NBT, Math.max(0, Math.min(value, this.getMaxOvercharge(stack))));
    }

    public int getMaxOvercharge(ItemStack stack) {
        return this.getEntry().getCost().getOvercharge().getMax();
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference) {
        if (clickType != ClickAction.SECONDARY) return false;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(other.getItem());
        if (this.getEntry().getCost().getOvercharge().getIngredients().containsKey(id)
                && this.getOvercharge(stack) < this.getMaxOvercharge(stack)) {

            other.shrink(1);
            this.setOvercharge(stack, this.getOvercharge(stack)
                    + this.getEntry().getCost().getOvercharge().getIngredients().get(id));

            return true;
        }
        return false;
    }
    //#endregion


    //#region Properties
    @Override
    public Map<ResourceLocation, Integer> getBuiltinEnchantments() {
        return this.getEntry().getEffects().getEnchants();
    }

    public int getCost(ItemStack stack, Player player, int original) {
        int wisdom = EnchantHelper.getEnchantLevel(ModEnchants.WISDOM, stack);
        int ignorance = EnchantHelper.getEnchantLevel(ModEnchants.IGNORANCE_CURSE, stack);

        return (int) (original * (1 - 0.25f * wisdom) + (ignorance == 0 ? 0 : Math.min(1, original * 0.25f * ignorance)));
    }

    public float getDamage(ItemStack stack) {
        return this.getEntry().getTier().getAttackDamageBonus() + EnchantHelper.getEnchantLevel(ModEnchants.POTENCY, stack);
    }

    public int getPiercing(ItemStack stack) {
        return this.getEntry().getTier().getPiercing() + EnchantHelper.getEnchantLevel(ModEnchants.CHAINING, stack);
    }

    public int getRange(ItemStack stack) {
        return this.getEntry().getTier().getRange() + EnchantHelper.getEnchantLevel(ModEnchants.STABILITY, stack)*2;
    }

    public int getCooldown(ItemStack stack, Player player) {
        float multiplier = 1 - EnchantHelper.getEnchantLevel(ModEnchants.FOCUS, stack) * 0.05f;

        if (stack.is(ModTags.Items.XP_BOOSTED) && player != null)
            multiplier -= player.experienceLevel / 200f;

        multiplier = Math.max(multiplier, 0);
        return (int) (this.getEntry().getTier().getCooldown() * multiplier);
    }

    public double getBlastRadius(ItemStack stack, LivingEntity player) {
        return EnchantHelper.getEnchantLevel(ModEnchants.BLAST, stack);
    }

    public List<float[]> getBeamColors(ItemStack stack, Set<ElementalStaffEnchantment> elements) {
        List<float[]> result = new ArrayList<>();

        int rgb = this.getColor(stack);
        float[] color = new float[]{FastColor.ARGB32.red(rgb) / 255f,
                FastColor.ARGB32.green(rgb) / 255f,
                FastColor.ARGB32.blue(rgb) / 255f};

        if (rgb == DyeableLeatherItem.DEFAULT_LEATHER_COLOR) {
            if (!this.getEntry().getDisplay().getColors().isEmpty())
                return this.getEntry().getDisplay().getColors();

            for (ElementalStaffEnchantment element : elements)
                result.addAll(element.colors);
            if (result.isEmpty())
                result.add(COLOR_NONE);
            if (stack.isEnchanted())
                result.add(COLOR_ENCHANTED);
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

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND && this.getEntry().getTier().getPiercing() > 0) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();

            if (this.canMelee(stack)) {
                builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,
                        "Weapon modifier", this.getDamage(stack)-1, AttributeModifier.Operation.ADDITION));
                builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID,
                        "Weapon modifier", -3, AttributeModifier.Operation.ADDITION));
            }

            builder.put(ModAttributes.STAFF_DAMAGE, new AttributeModifier(ModAttributes.STAFF_DAMAGE.getUUID(),
                    "Staff modifier", this.getDamage(stack), AttributeModifier.Operation.ADDITION));
            builder.put(ModAttributes.STAFF_PIERCE, new AttributeModifier(ModAttributes.STAFF_PIERCE.getUUID(),
                    "Staff modifier", this.getPiercing(stack), AttributeModifier.Operation.ADDITION));
            builder.put(ModAttributes.STAFF_RANGE, new AttributeModifier(ModAttributes.STAFF_RANGE.getUUID(),
                    "Staff modifier", this.getRange(stack), AttributeModifier.Operation.ADDITION));

            return builder.build();
        }
        return super.getDefaultAttributeModifiers(slot);
    }
    //#endregion


    //#region Implementation
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.isCreative() && !canShoot(stack, player))
            return InteractionResultHolder.pass(stack);
        if (player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND).getItem() instanceof ShieldItem
                && player.isShiftKeyDown())
            return InteractionResultHolder.pass(stack);

        this.hand = hand;
        player.startUsingItem(hand);
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
    }

    public void runCommand(LivingEntity origin, @Nullable String command) {
        if (origin.level() instanceof ServerLevel server && command != null) {
            server.getServer().getCommands().performPrefixedCommand(
                    origin.createCommandSourceStack()
                            .withMaximumPermission(Commands.LEVEL_OWNERS)
                            .withSuppressedOutput(), command);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (!(entity instanceof Player player)) return stack;

        this.displayShot(stack, player);
        this.onShoot(stack, player);
        this.applyCost(stack, player);

        Set<ElementalStaffEnchantment> elements = this.getElements(stack);
        List<float[]> colors = this.getBeamColors(stack, elements);
        Vec3 look = MathHelper.getLookVector(player);

        this.shoot(stack, player, elements, colors, look, new ArrayList<>());
        return stack;
    }

    public float modifyDamageDealt(ItemStack stack, float damage, LivingEntity target, Set<ElementalStaffEnchantment> elements) {
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

    public void triggerAttack(ItemStack stack, LivingEntity player, LivingEntity target, Set<ElementalStaffEnchantment> elements,
                              Vec3 direction, boolean propagate, List<LivingEntity> targetsHit) {

        if (targetsHit.contains(target.getUUID())) return;

        Level world = player.level();
        float kinesis = EnchantHelper.getEnchantLevel(ModEnchants.PUSH, stack) - EnchantHelper.getEnchantLevel(ModEnchants.PULL, stack);
        float d = this.modifyDamageDealt(stack, this.getDamage(stack), target, elements);

        if (d > 0)
            target.hurt(player.damageSources().indirectMagic(player, player), d);
        else if (d < 0) {
            target.heal(-d);
            ModParticles.spawnWisps(world, target.getX(), target.getY() + target.getEyeHeight() / 2, target.getZ(),
                    10, new float[]{1, 0.5f, 0.5f});
        }

        targetsHit.add(target);

        this.onHit(stack, player, target);
        if (target.isDeadOrDying()) this.onKill(stack, player, target);

        if (kinesis != 0)
            target.setDeltaMovement(direction.add(0, 0.07, 0).normalize().scale(kinesis * 0.55));

        for (ElementalStaffEnchantment element : elements) {
            int elementLevel = EnchantHelper.getEnchantLevel(element, stack);

            element.triggerAttack(target, elementLevel);
        }
        if (propagate) {
            this.triggerBlastAttack(stack, player, elements, direction,
                    target.getX(), target.getY() + target.getEyeHeight() / 2, target.getZ(), targetsHit);
        }
    }

    public void triggerBlastAttack(ItemStack stack, LivingEntity player, Set<ElementalStaffEnchantment> elements, Vec3 direction,
                                   double x, double y, double z, List<LivingEntity> targetsHit) {

        double radius = this.getBlastRadius(stack, player);
        if (radius <= 0) return;

        if (player.level().isClientSide())
            player.level().explode(player, x, y, z, 1, Level.ExplosionInteraction.NONE);

        Vec3 pos = new Vec3(x, y, z);
        Vec3 offset = new Vec3(radius, radius, radius);

        for (Entity entity : player.level().getEntities(player, new AABB(pos.subtract(offset), pos.add(offset)))) {
            if (entity instanceof LivingEntity target && this.canHit(stack, player, target))
                this.triggerAttack(stack, player, target, elements, direction, false, targetsHit);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity player) {
        if (this.canMelee(stack)) {
            this.triggerAttack(stack, player, target, this.getElements(stack), MathHelper.getLookVector(player),
                    true, new ArrayList<>());
        }
        return super.hurtEnemy(stack, target, player);
    }
    //#endregion


    //#region Abstraction
    public boolean canShoot(ItemStack stack, Player player) {
        return (this.getOvercharge(stack) > 0 && this.getEntry().getCost().getOvercharge().ignoreCost()) || this.hasResource(stack, player);
    }

    public abstract boolean hasResource(ItemStack stack, Player player);

    public void applyCost(ItemStack stack, Player player) {
        if (this.getOvercharge(stack) <= 0 || !this.getEntry().getCost().getOvercharge().ignoreDurability())
            stack.hurtAndBreak(1, player, e -> e.broadcastBreakEvent(this.hand));
        if (this.getOvercharge(stack) <= 0 || !this.getEntry().getCost().getOvercharge().ignoreCost())
            this.consumeResource(stack, player);

        if (this.getOvercharge(stack) > 0)
            this.setOvercharge(stack, this.getOvercharge(stack) - 1);

        player.getCooldowns().addCooldown(this, this.getCooldown(stack, player));
    }

    public abstract void consumeResource(ItemStack stack, Player player);

    public void onShoot(ItemStack stack, Player player) {
        this.runCommand(player, this.getEntry().getEffects().onShoot());
    }

    public void shoot(ItemStack stack, Player player, Set<ElementalStaffEnchantment> elements, List<float[]> colors,
                      Vec3 direction, List<LivingEntity> targetsHit) {

        int targetsLeft = this.getPiercing(stack);
        int index;

        float x = (float) player.getX();
        float y = (float) player.getY();
        float z = (float) player.getZ();
        BlockPos pos;

        int step = 5;
        // Main loop, displaying particles and hurting mobs on its way
        for (int i = 1; i < this.getRange(stack) * step; i++) {
            x = (float) (player.getX() + direction.x * i/step);
            y = (float) (player.getY() + direction.y * i/step + player.getEyeHeight(player.getPose()) - 0.2);
            z = (float) (player.getZ() + direction.z * i/step);

            this.displayBeam(player, x, y, z, colors);

            if (i % step != 0) continue;

            pos = new BlockPos((int) Math.round(x-0.5), (int) Math.round(y-0.5), (int) Math.round(z-0.5));
            Vec3 vec = new Vec3(x, y, z).subtract(pos.getX(), pos.getY(), pos.getZ());
            List<Entity> entities = player.level().getEntities(player, new AABB(pos).inflate(0.1));

            if (player.level().getBlockState(pos).getCollisionShape(player.level(), pos).toAabbs()
                    .stream().anyMatch(box -> box.contains(vec))) {
                if (ModConfig.staffsPierceBlocks.get())
                    targetsLeft--;
                else
                    break;
            }
            if (targetsLeft <= 0)
                break;

            index = 0;
            while (!entities.isEmpty() && entities.size() > index && targetsLeft > 0) {
                if (entities.get(index) instanceof LivingEntity target
                        && !targetsHit.contains(target) && this.canHit(stack, player, target)) {

                    this.triggerAttack(stack, player, target, elements, direction, true, targetsHit);
                    targetsLeft--;
                }
                index++;
            }
        }

        this.triggerBlastAttack(stack, player, elements, direction, x, y, z, targetsHit);
    }

    public boolean canHit(ItemStack stack, LivingEntity player, LivingEntity target) {
        return !(target instanceof OwnableEntity tameable && tameable.getOwner() == player) && !target.getPassengers().contains(player);
    }

    public void onHit(ItemStack stack, LivingEntity player, LivingEntity target) {
        this.runCommand(player, this.getEntry().getEffects().onHitSelf());
        this.runCommand(target, this.getEntry().getEffects().onHitTarget());
    }

    public void onKill(ItemStack stack, LivingEntity player, LivingEntity target) {}

    public boolean canMelee(ItemStack stack) {
        return EnchantHelper.hasEnchant(ModEnchants.BONK, stack);
    }

    public void displayShot(ItemStack stack, Player player) {
        if (this.hand != null) player.swing(this.hand, true);

        if (this.getEntry().getDisplay().getSound() != null)
            player.level().playSound(player, player.blockPosition(), this.getEntry().getDisplay().getSound(), SoundSource.PLAYERS, 1, 1);
    }

    public ParticleType<?> getParticle() {
        if (this.getEntry().getDisplay().getParticle() == null) return ModParticles.WISP_PIXEL;
        ParticleType<?> particle = BuiltInRegistries.PARTICLE_TYPE.get(this.getEntry().getDisplay().getParticle());
        return particle == null ? ModParticles.WISP_PIXEL : particle;
    }

    public void displayBeam(Player player, float x, float y, float z, List<float[]> colors) {
        ParticleType<?> particle = this.getParticle();
        float[] color = MathHelper.randi(colors);

        if (particle == ModParticles.WISP_PIXEL) {
            ModParticles.spawnWisps(player.level(), x, y, z, 1, color);
            return;
        }

        try {
            if (player.level().isClientSide() && particle == BotaniaParticles.WISP) {
                float r = color[0], g = color[1], b = color[2];
                boolean depth = !ManaseerMonocleItem.hasMonocle(player);

                if (BotaniaConfig.client().subtlePowerSystem()) {
                    WispParticleData data = WispParticleData.wisp(0.1f, r, g, b, 0.5f, depth);
                    Proxy.INSTANCE.addParticleForceNear(player.level(), data, x, y, z,
                            (Math.random() - 0.5) * 0.02, (Math.random() - 0.5) * 0.02, (Math.random() - 0.5) * 0.02);
                } else {
                    float or = r;
                    float og = g;
                    float ob = b;
                    double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;

                    WispParticleData data;
                    if (luminance < 0.1) {
                        r = or + (float) Math.random() * 0.125f;
                        g = og + (float) Math.random() * 0.125f;
                        b = ob + (float) Math.random() * 0.125f;
                    }

                    float size = (float) (1 + (Math.random() - 0.5) * 0.065 + Math.sin(new Random(player.getUUID().getMostSignificantBits()).nextInt(9001)) * 0.4);
                    data = WispParticleData.wisp(0.2f * size, r, g, b, 0.3f, depth);
                    Proxy.INSTANCE.addParticleForceNear(player.level(), data, x, y, z, 0, 0, 0);

                    data = WispParticleData.wisp(0.1f * size, or, og, ob, 0.3f, depth);
                    player.level().addParticle(data, x, y, z, (float) (Math.random() - 0.5) * 0.06f, (float) (Math.random() - 0.5) * 0.06f, (float) (Math.random() - 0.5) * 0.06f);
                }
            }
            return;
        } catch (Throwable ignored) {}

        if (particle instanceof ParticleOptions options)
            player.level().addAlwaysVisibleParticle(options, x, y, z, color[0], color[1], color[2]);
    }

    @Environment(EnvType.CLIENT)
    public void appendTooltipAbilities(ItemStack stack, Player player, List<Component> tooltip) {
        // Undergarden compat
        if (stack.is(ModTags.Items.FROSTSTEEL_ITEMS))
            tooltip.add(Component.translatable("tooltip.froststeel_sword").withStyle(ChatFormatting.AQUA));
        if (stack.is(ModTags.Items.UTHERIUM_ITEMS))
            tooltip.add(Component.translatable("tooltip.utheric_sword").withStyle(ChatFormatting.RED));
        if (stack.is(ModTags.Items.FORGOTTEN_ITEMS))
            tooltip.add(Component.translatable("tooltip.forgotten_sword").withStyle(ChatFormatting.GREEN));
    }

    @Environment(EnvType.CLIENT)
    public void appendTooltipCosts(ItemStack stack, Player player, List<Component> tooltip) {
        tooltip.add(Component.translatable("sortilege.staff.cooldown", this.getCooldown(stack, Minecraft.getInstance().player) / 20f).withStyle(ChatFormatting.GRAY));
    }
    //#endregion
}