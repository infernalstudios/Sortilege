package net.lyof.sortilege.items.custom;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.CustomStaffHandler;
import net.lyof.sortilege.configs.ModCommonConfigs;
import net.lyof.sortilege.configs.ModStaffConfigs;
import net.lyof.sortilege.enchants.ModEnchants;
import net.lyof.sortilege.items.ModItems;
import net.lyof.sortilege.utils.ItemHelper;
import net.lyof.sortilege.utils.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class StaffItem extends TieredItem {
    public float attackDamage;
    public int maxTargets;
    public int attackRange;

    public StaffItem(ModStaffConfigs.Staff stats) {
        this(stats.tier, stats.damage, stats.pierce, stats.range, stats.durability,
                stats.fireRes ?
                    new Item.Properties().tab(CreativeModeTab.TAB_COMBAT).fireResistant() :
                    new Item.Properties().tab(CreativeModeTab.TAB_COMBAT));
    }

    public StaffItem(Tier pTier, int damage, int targets, int range, Properties properties) {
        this(pTier, (int) (damage + pTier.getAttackDamageBonus()), targets, range, (int) Math.round(pTier.getUses() * 0.7), properties);
    }

    public StaffItem(Tier pTier, int damage, int targets, int range, int dura, Properties properties) {
        super(pTier, properties.defaultDurability(dura));

        this.attackDamage = damage;
        this.maxTargets = targets;
        this.attackRange = range;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        list.add(Component.literal(this.attackDamage + " Magic Damage"));
        list.add(Component.literal(this.maxTargets + " Pierce"));
        list.add(Component.literal(this.attackRange + " Range"));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        int ATTACK_COOLDOWN = ModCommonConfigs.STAFF_ATTACK_COOLDOWN.get();
        int NEED_XP = ModCommonConfigs.STAFF_XP_COST.get();


        ItemStack staff = player.getItemInHand(hand);

        int ench_brazier = ItemHelper.getEnchantLevel(ModEnchants.BRAZIER, staff);
        int ench_blizzard = ItemHelper.getEnchantLevel(ModEnchants.BLIZZARD, staff);
        int ench_ignorance = ItemHelper.getEnchantLevel(ModEnchants.IGNORANCE_CURSE, staff) + NEED_XP;
        int ench_potency = ItemHelper.getEnchantLevel(ModEnchants.POTENCY, staff);
        int range = this.attackRange + ItemHelper.getEnchantLevel(ModEnchants.STABILITY, staff)*2;


        if (ench_ignorance > 0 && !player.isCreative()) {
            if (player.totalExperience < ench_ignorance)
                return super.use(world, player, hand);
            player.giveExperiencePoints(-ench_ignorance);
        }

        player.swing(hand, true);
        world.playSound(player, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1, 1);
        player.getCooldowns().addCooldown(staff.getItem(), ATTACK_COOLDOWN);
        if (!player.getAbilities().instabuild && staff.hurt(1, RandomSource.create(), null)) {
            staff.shrink(1);
            staff.setDamageValue(0);
        }


        // Getting the look vector to shoot the ray along
        Vec3 look = MathHelper.getLookVector(player);


        // Initialising variables to be used in the loop
        int targetsLeft = this.maxTargets;// + ItemHelper.getEnchantLevel(ModEnchants.CHAINING, staff);
        List<String> targetsHit = new ArrayList<>();
        int index;

        float x;
        float y;
        float z;
        BlockPos pos;

        DamageSource damagetype = DamageSource.indirectMagic(player, player);

        ParticleOptions particle;
        if (ench_brazier > 0) {
            particle = ParticleTypes.FLAME;
            damagetype.setIsFire();
        }
        else if (ench_blizzard > 0)
            particle = ParticleTypes.SNOWFLAKE;
        else
            particle = ParticleTypes.INSTANT_EFFECT;


        // Main loop, displaying particles and hurting mobs on its way
        for (int i = 1; i < range * 2; i++) {
            x = (float) (player.getX() + look.x * i/2);
            y = (float) (player.getY() + look.y * i/2 + player.getEyeHeight());
            z = (float) (player.getZ() + look.z * i/2);

            player.getLevel().addParticle(particle, x, y ,z, 0, 0, 0);
            if (staff.isEnchanted() && Math.random() < 0.5)
                player.getLevel().addParticle(ParticleTypes.ENCHANTED_HIT, x, y ,z, 0, 0, 0);

            pos = new BlockPos(Math.round(x-0.5), Math.round(y-0.5), Math.round(z-0.5));
            List<Entity> entities = player.getLevel().getEntities(null, new AABB(pos).inflate(0.5));

            if (targetsLeft <= 0 || player.level.getBlockState(pos).canOcclude())
                break;

            index = 0;
            while (!entities.isEmpty() && entities.size() > index
                    && !entities.get(index).equals(player) && targetsLeft > 0) {

                if (entities.get(index) instanceof LivingEntity target
                        && !targetsHit.contains(target.getStringUUID())) {
                    target.hurt(damagetype, this.attackDamage + ench_potency);

                    if (ench_brazier > 0)
                        target.setSecondsOnFire(ench_brazier * 4);
                    else if (ench_blizzard > 0) {
                        target.setTicksFrozen(target.getTicksFrozen() + 150*ench_blizzard);
                    }

                    targetsHit.add(target.getStringUUID());
                    targetsLeft--;
                }
                index++;
            }
        }
        return super.use(world, player, hand);
    }
}