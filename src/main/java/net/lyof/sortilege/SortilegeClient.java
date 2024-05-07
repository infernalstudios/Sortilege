package net.lyof.sortilege;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.potion.AntidotePotionItem;
import net.minecraft.client.render.model.SpriteAtlasManager;

public class SortilegeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.ITEM.register(AntidotePotionItem::getItemColor, ModItems.ANTIDOTE);


    }
}
