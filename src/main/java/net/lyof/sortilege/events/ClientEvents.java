package net.lyof.sortilege.events;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.items.ModItems;
import net.lyof.sortilege.items.armor.rendering.WitchHatModel;
import net.lyof.sortilege.items.custom.potion.AntidotePotionItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Sortilege.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WitchHatModel.LAYER_LOCATION, WitchHatModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerItemColor(RegisterColorHandlersEvent.Item event) {
        event.getItemColors().register(AntidotePotionItem::getItemColor, ModItems.ANTIDOTE.get());
    }
}
