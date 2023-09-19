package net.lyof.sortilege.events;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.lyof.sortilege.attributes.ModAttributes;
import net.lyof.sortilege.configs.ModJsonConfigs;
import net.lyof.sortilege.items.custom.StaffItem;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.function.Consumer;

@Mod.EventBusSubscriber
public class SetupEvents {
    @SubscribeEvent
    public static void entityAttributeProvider(EntityAttributeModificationEvent event){
        event.add(EntityType.PLAYER, ModAttributes.STAFF_DAMAGE.get());
        event.add(EntityType.PLAYER, ModAttributes.STAFF_PIERCE.get());
        event.add(EntityType.PLAYER, ModAttributes.STAFF_RANGE.get());
    }

    @SubscribeEvent
    public static void itemEvent(ItemAttributeModifierEvent event) {
        ItemStack itemStack = event.getItemStack();
        Item item = itemStack.getItem();

        if (!(item instanceof StaffItem staff)) return;
        if (event.getSlotType() != EquipmentSlot.MAINHAND) return;

        event.addModifier(ModAttributes.STAFF_DAMAGE.get(),
                new AttributeModifier(ModAttributes.DAMAGE_UUID, "Staff modifier",
                        staff.damage, AttributeModifier.Operation.ADDITION));
        event.addModifier(ModAttributes.STAFF_PIERCE.get(),
                new AttributeModifier(ModAttributes.PIERCE_UUID, "Staff modifier",
                        staff.pierce, AttributeModifier.Operation.ADDITION));
        event.addModifier(ModAttributes.STAFF_RANGE.get(),
                new AttributeModifier(ModAttributes.RANGE_UUID, "Staff modifier",
                        staff.range, AttributeModifier.Operation.ADDITION));
    }

    @SubscribeEvent
    public static void reloadConfigs(PlayerEvent.PlayerLoggedInEvent event) {
        ModJsonConfigs.register();
    }
}
