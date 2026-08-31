package net.lyof.sortilege.item.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lcc.sollib.api.client.render.MockItemRenderer;
import net.lcc.sollib.api.client.render.item.IAddedBarItem;
import net.lcc.sollib.api.client.render.item.IAddedRenderItem;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.attribute.ModAttributes;
import net.lyof.sortilege.enchant.IBuiltinEnchantsItem;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.enchant.custom.StaffColorsEnchant;
import net.lyof.sortilege.enchant.custom.StaffStatsEnchant;
import net.lyof.sortilege.item.ModDataComponents;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.EnchantHelper;
import net.lyof.sortilege.util.MathHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AStaffItem extends TieredItem implements IAddedRenderItem, IAddedBarItem, IBuiltinEnchantsItem {
    private static final int COLOR_NONE = 0xffffff;
    private static final int COLOR_ENCHANTED = 0x9900ff;

    private static final ResourceLocation COLOR_OVERLAY = Sortilege.MOD.makeID("textures/models/staff/glint.png");

    protected final StaffEntry entry;
    protected String name;
    protected InteractionHand hand;

    public AStaffItem(StaffEntry entry, Properties properties) {
        super(entry.getTier(), entry.applyProperties(properties).component(ModDataComponents.OVERCHARGE, 0));
        this.entry = entry;
        this.setName(entry.getID());
    }

    public StaffEntry getEntry() {
        return this.entry;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    //#region Rendering
    @Override
    public boolean shouldRender(ItemStack stack) {
        return DyedItemColor.getOrDefault(stack, -1) != -1 && !stack.is(ModTags.Items.NO_DYE_OVERLAY_STAFFS);
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext context, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        matrices.scale(1.005f, 1.005f, 1.005f);
        matrices.translate(0, 0.995, 0.4975);
        matrices.mulPose(Axis.XP.rotationDegrees(180));

        MockItemRenderer.renderTintedItem(matrices, vertexConsumers, light, COLOR_OVERLAY, DyedItemColor.getOrDefault(stack, -1));
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
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return this.getEntry().getTier().getChargeTime();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            int i = tooltip.size();
            super.appendHoverText(stack, context, tooltip, flag);

            this.appendTooltipAbilities(stack, Minecraft.getInstance().player, tooltip);
            if (i < tooltip.size())
                tooltip.add(Component.empty());

            i = tooltip.size();
            this.appendTooltipCosts(stack, Minecraft.getInstance().player, tooltip);
            if (i < tooltip.size()) tooltip.add(Component.empty());
        } else tooltip.add(EnchantHelper.getShiftTooltip());
    }
    //#endregion


    //#region Overcharge
    public int getOvercharge(ItemStack stack) {
        return stack.get(ModDataComponents.OVERCHARGE);
    }

    public void setOvercharge(ItemStack stack, int value) {
        stack.set(ModDataComponents.OVERCHARGE, Math.max(0, Math.min(value, this.getMaxOvercharge(stack))));
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

            ItemStack remainder = other.getRecipeRemainder();
            if (!remainder.isEmpty()) {
                if (other.getCount() == 1)
                    cursorStackReference.set(remainder);
                else {
                    player.addItem(remainder);
                    other.shrink(1);
                }
            } else other.shrink(1);

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
        //int wisdom = EnchantHelper.getEnchantLevel(ModEnchants.WISDOM, stack);
        //int ignorance = EnchantHelper.getEnchantLevel(ModEnchants.IGNORANCE_CURSE, stack);
        return original;
        //return (int) (original * (1 - 0.25f * wisdom) + (ignorance == 0 ? 0 : Math.min(1, original * 0.25f * ignorance)));
    }

    public float getDamage(ItemStack stack) {
        return this.getEntry().getTier().getAttackDamageBonus() + StaffStatsEnchant.collect(stack.getEnchantments()).damage();
    }

    public float getDamage(ItemStack stack, LivingEntity player) {
        return this.getDamage(stack) + (float) player.getAttributeValue(ModAttributes.STAFF_DAMAGE);
    }

    public int getPiercing(ItemStack stack) {
        return this.getEntry().getTier().getPiercing() + StaffStatsEnchant.collect(stack.getEnchantments()).pierce();
    }

    public int getPiercing(ItemStack stack, LivingEntity player) {
        return this.getPiercing(stack) + (int) player.getAttributeValue(ModAttributes.STAFF_PIERCE);
    }

    public int getRange(ItemStack stack) {
        return this.getEntry().getTier().getRange() + StaffStatsEnchant.collect(stack.getEnchantments()).range();
    }

    public int getRange(ItemStack stack, LivingEntity player) {
        return this.getRange(stack) + (int) player.getAttributeValue(ModAttributes.STAFF_RANGE);
    }

    public int getCooldown(ItemStack stack, Player player) {
        float multiplier = 1/* - EnchantHelper.getEnchantLevel(ModEnchants.FOCUS, stack) * 0.05f*/;

        if (stack.is(ModTags.Items.XP_BOOSTED) && player != null)
            multiplier -= player.experienceLevel / 200f;

        multiplier = Math.max(multiplier, 0);
        return (int) (this.getEntry().getTier().getCooldown() * multiplier);
    }

    public double getBlastRadius(ItemStack stack, LivingEntity player) {
        return 0;// EnchantHelper.getEnchantLevel(ModEnchants.BLAST, stack);
    }

    public List<Integer> getBeamColors(ItemStack stack) {
        List<Integer> result = new ArrayList<>();

        int rgb = DyedItemColor.getOrDefault(stack, -1);

        if (rgb == -1) {
            if (!this.getEntry().getDisplay().getColors().isEmpty())
                return this.getEntry().getDisplay().getColors();

            result = StaffColorsEnchant.collect(stack.getEnchantments());
            if (!result.isEmpty()) return result;

            result.add(COLOR_NONE);
            if (stack.isEnchanted())
                result.add(COLOR_ENCHANTED);
        }
        else {
            float[] color = new float[]{FastColor.ARGB32.red(rgb) / 255f,
                    FastColor.ARGB32.green(rgb) / 255f,
                    FastColor.ARGB32.blue(rgb) / 255f};

            for (int i = 0 ; i < 5; i++) {
                result.add(FastColor.ARGB32.colorFromFloat(1, (float) (color[0] + Math.random()*0.1 - 0.05),
                        (float) (color[1] + Math.random()*0.1 - 0.05),
                        (float) (color[2] + Math.random()*0.1 - 0.05)));
            }
        }

        return result;
    }

    public ItemAttributeModifiers getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        if (this.canMelee(stack)) {
            builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID,
                    this.getDamage(stack)-1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID,
                    -3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        }

        this.addAttributeModifiers(stack, builder);

        return builder.build();
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

    public String processCommand(ItemStack stack, LivingEntity origin, String command) {
        Vec3 look = origin.getLookAngle().scale(0.1);

        return command.replace("{direction.x}", String.valueOf(look.x))
                .replace("{direction.y}", String.valueOf(look.y))
                .replace("{direction.z}", String.valueOf(look.z));
    }

    public void runCommand(ItemStack stack, LivingEntity origin, @Nullable String command) {
        if (origin.level() instanceof ServerLevel server && command != null) {
            server.getServer().getCommands().performPrefixedCommand(
                    origin.createCommandSourceStack()
                            .withMaximumPermission(Commands.LEVEL_OWNERS)
                            .withSuppressedOutput(), this.processCommand(stack, origin, command));
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (!(entity instanceof Player player)) return stack;

        Vec3 look = player.getLookAngle();

        this.displayShot(stack, player);
        this.onShoot(stack, player);
        this.applyCost(stack, player);

        this.shoot(stack, player, look, new ArrayList<>());
        return stack;
    }

    public float modifyDamageDealt(ItemStack stack, float damage, LivingEntity player, LivingEntity target) {
        /*if (elements.contains((Object) ModEnchants.BLESSING)) {
            if (target.getType().is(ModTags.Entities.UNDEAD))
                damage *= 1 + EnchantHelper.getEnchantLevel(ModEnchants.BLESSING, stack) * 0.5f;
            else if (!ModConfig.altBlessing.get() || !(target instanceof Enemy))
                damage *= EnchantHelper.getEnchantLevel(ModEnchants.BLESSING, stack) * -0.75f;
        }*/

        // Undergarden compat
        if (target.getType().is(ModTags.Entities.UNDERGARDEN_ENTITIES) && stack.is(ModTags.Items.FORGOTTEN_ITEMS))
            damage *= 1.5f;
        if (target.getType().is(ModTags.Entities.ROTSPAWN) && stack.is(ModTags.Items.UTHERIUM_ITEMS))
            damage *= 1.5f;

        return damage;
    }

    public void triggerAttack(ItemStack stack, LivingEntity player, LivingEntity target,
                              Vec3 direction, boolean propagate, List<LivingEntity> targetsHit) {
        if (targetsHit.contains(target)) return;

        Level world = player.level();
        float kinesis = 0;//EnchantHelper.getEnchantLevel(ModEnchants.PUSH, stack) - EnchantHelper.getEnchantLevel(ModEnchants.PULL, stack);
        float d = this.getDamage(stack, player);
        d = this.modifyDamageDealt(stack, d, player, target);

        if (d > 0)
            target.hurt(player.damageSources().indirectMagic(player, player), d);
        else if (d < 0) {
            target.heal(-d);
            ModParticles.sendParticles(world, target.getX(), target.getY() + target.getEyeHeight() / 2, target.getZ(),
                    10, 0xff8888);
        }

        this.onHit(stack, player, target);
        if (target.isDeadOrDying()) this.onKill(stack, player, target);

        targetsHit.add(target);

        /*if (kinesis != 0)
            target.setDeltaMovement(direction.add(0, 0.07, 0).normalize().scale(kinesis * 0.55));*/

        if (propagate) {
            this.triggerBlastAttack(stack, player, direction,
                    target.getX(), target.getY() + target.getEyeHeight() / 2, target.getZ(), targetsHit);
        }
    }

    public void triggerBlastAttack(ItemStack stack, LivingEntity player, Vec3 direction,
                                   double x, double y, double z, List<LivingEntity> targetsHit) {

        double radius = this.getBlastRadius(stack, player);
        if (radius <= 0) return;

        if (player.level().isClientSide())
            player.level().explode(player, x, y, z, 1, Level.ExplosionInteraction.NONE);

        Vec3 pos = new Vec3(x, y, z);
        Vec3 offset = new Vec3(radius, radius, radius);

        for (Entity entity : player.level().getEntities(player, new AABB(pos.subtract(offset), pos.add(offset)))) {
            if (entity instanceof LivingEntity target && this.canHit(stack, player, target))
                this.triggerAttack(stack, player, target, direction, false, targetsHit);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity player) {
        if (this.canMelee(stack)) {
            this.triggerAttack(stack, player, target, player.getLookAngle(),
                    true, new ArrayList<>());

            if (player instanceof Player p)
                p.getCooldowns().addCooldown(this, this.getCooldown(stack, p));
        }
        return super.hurtEnemy(stack, target, player);
    }
    //#endregion


    //#region Abstraction
    public boolean canShoot(ItemStack stack, Player player) {
        return (this.getOvercharge(stack) > 0 && this.getEntry().getCost().getOvercharge().ignoresCost())
                || (!this.getEntry().getCost().getOvercharge().isRequired() && this.hasResource(stack, player));
    }

    public abstract boolean hasResource(ItemStack stack, Player player);

    public void applyCost(ItemStack stack, Player player) {
        player.getCooldowns().addCooldown(this, this.getCooldown(stack, player));

        if (player.isCreative()) return;

        if (this.getOvercharge(stack) <= 0 || !this.getEntry().getCost().getOvercharge().ignoresDurability())
            stack.hurtAndBreak(1, player, this.hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        if (this.getOvercharge(stack) <= 0 || !this.getEntry().getCost().getOvercharge().ignoresCost())
            this.consumeResource(stack, player);

        if (this.getOvercharge(stack) > 0)
            this.setOvercharge(stack, this.getOvercharge(stack) - 1);
    }

    public abstract void consumeResource(ItemStack stack, Player player);

    public void onShoot(ItemStack stack, Player player) {
        this.runCommand(stack, player, this.getEntry().getEffects().onShoot());
    }

    public void shoot(ItemStack stack, Player player, Vec3 direction, List<LivingEntity> targetsHit) {
        List<Integer> colors = this.getBeamColors(stack);
        int targetsLeft = this.getPiercing(stack, player);
        int index;

        float x = (float) player.getX();
        float y = (float) player.getY();
        float z = (float) player.getZ();
        BlockPos pos;

        int step = 5;
        // Main loop, displaying particles and hurting mobs on its way
        for (int i = 1; i < this.getRange(stack, player) * step; i++) {
            x = (float) (player.getX() + direction.x * i/step);
            y = (float) (player.getY() + direction.y * i/step + player.getEyeHeight(player.getPose()) - 0.2);
            z = (float) (player.getZ() + direction.z * i/step);

            this.displayBeam(stack, player, x, y, z, colors);

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
            while (entities.size() > index && targetsLeft > 0) {
                if (entities.get(index) instanceof LivingEntity target
                        && !targetsHit.contains(target) && this.canHit(stack, player, target)) {

                    this.triggerAttack(stack, player, target, direction, true, targetsHit);
                    targetsLeft--;
                }
                index++;
            }
        }

        this.triggerBlastAttack(stack, player, direction, x, y, z, targetsHit);
    }

    public boolean canHit(ItemStack stack, LivingEntity player, LivingEntity target) {
        return !(target instanceof OwnableEntity tameable && tameable.getOwner() == player) && !target.getPassengers().contains(player);
    }

    public void onHit(ItemStack stack, LivingEntity player, LivingEntity target) {
        if (player.level() instanceof ServerLevel serverWorld) {
            for (Object2IntMap.Entry<Holder<Enchantment>> enchant : stack.getEnchantments().entrySet()) {
                LootContext context = new LootContext.Builder(new LootParams.Builder(serverWorld)
                        .withParameter(LootContextParams.THIS_ENTITY, target)
                        .withParameter(LootContextParams.ENCHANTMENT_LEVEL, enchant.getIntValue())
                        .withParameter(LootContextParams.ORIGIN, target.position())
                        .withParameter(LootContextParams.DAMAGE_SOURCE, player.damageSources().indirectMagic(player, player))
                        .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, player)
                        .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, player)
                        .create(LootContextParamSets.ENCHANTED_DAMAGE)).create(Optional.empty());

                for (TargetedConditionalEffect<EnchantmentEntityEffect> effect : enchant.getKey().value().getEffects(ModEnchants.POST_STAFF_HIT)) {
                    if (effect.matches(context))
                        effect.effect().apply(serverWorld, enchant.getIntValue(),
                                new EnchantedItemInUse(stack, this.hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND, player),
                                target, target.position());
                }
            }
        }

        this.runCommand(stack, player, this.getEntry().getEffects().onHitSelf());
        this.runCommand(stack, target, this.getEntry().getEffects().onHitTarget());
    }

    public void onKill(ItemStack stack, LivingEntity player, LivingEntity target) {}

    public boolean canMelee(ItemStack stack) {
        return false;// EnchantHelper.hasEnchant(ModEnchants.BONK, stack);
    }

    public boolean shouldDisplayAttributes(ItemStack stack, Player player) {
        return this.getRange(stack) > 0 || this.getDamage(stack) > 0 || this.getPiercing(stack) > 0;
    }

    public void addAttributeModifiers(ItemStack stack, ItemAttributeModifiers.Builder builder) {}

    public void displayShot(ItemStack stack, Player player) {
        if (this.hand != null) player.swing(this.hand, true);

        if (this.getEntry().getDisplay().getSound() != null)
            player.level().playSound(player, player.blockPosition(), this.getEntry().getDisplay().getSound(), SoundSource.PLAYERS, 1, 1);
    }

    public ResourceLocation getParticle(ItemStack stack) {
        if (this.getEntry().getDisplay().getParticle() == null) return ModParticles.WISP_ID;
        return this.getEntry().getDisplay().getParticle();
    }

    public void displayBeam(ItemStack stack, Player player, float x, float y, float z, List<Integer> colors) {
        ModParticles.sendParticles(player.level(), this.getParticle(stack), x, y, z, 1, MathHelper.randi(colors));
    }

    @Environment(EnvType.CLIENT)
    public void appendTooltipAbilities(ItemStack stack, Player player, List<Component> tooltip) {
        Component desc = Component.translatableWithFallback(this.getDescriptionId() + ".desc", "");
        if (!desc.getString().isEmpty()) {
            for (String s : desc.getString().split("\n"))
                tooltip.add(Component.literal(s).withStyle(ChatFormatting.GRAY));
        }

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
        tooltip.add(Component.translatable("tooltip.sortilege.staff.cooldown", this.getCooldown(stack, Minecraft.getInstance().player) / 20f).withStyle(ChatFormatting.GRAY));
    }
    //#endregion
}