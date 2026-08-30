package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.lcc.sollib.core.Identifier;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

@Mixin(ItemModelShaper.class)
public class ItemModelShaperMixin {
    @Unique private static final Map<String, BakedModel> MODEL_CACHE = new HashMap<>();
    @Unique private static final Map<String, ModelResourceLocation> ID_CACHE = new HashMap<>();

    @Shadow @Final private ModelManager modelManager;

    @WrapMethod(method = "getItemModel(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/resources/model/BakedModel;")
    public BakedModel getCustomModel(ItemStack stack, Operation<BakedModel> original) {
        if (!ModConfig.customPotionTextures.get()) return original.call(stack);
        if (!PotionHelper.isPotionItem(stack)) return original.call(stack);
        PotionContents component = stack.get(DataComponents.POTION_CONTENTS);
        if (component == null || component.potion().isEmpty()) return original.call(stack);
        
        ResourceLocation id = Identifier.of(component.potion().get().getRegisteredName());
        String key = PotionHelper.getPotionItemType(stack);
        if (MODEL_CACHE.containsKey(key)) return MODEL_CACHE.get(key);

        String base = "";
        if (stack.is(Items.SPLASH_POTION)) base = "splash/";
        else if (stack.is(Items.LINGERING_POTION)) base = "lingering/";

        BakedModel model = this.modelManager.getModel(sorti_getId(id.getNamespace(),
                "potions/" + base + id.getPath()));

        if (model == this.modelManager.getMissingModel()) {
            String type = "";
            if (id.getPath().startsWith("strong_") || id.getPath().endsWith("_strong"))
                type = "strong_";
            else if (id.getPath().startsWith("long_") || id.getPath().endsWith("_long"))
                type = "long_";

            model = this.modelManager.getModel(sorti_getId("minecraft",
                    type + base.replace("/", "_") + "potion"));
        }

        if (model == this.modelManager.getMissingModel())
            model = original.call(stack);

        MODEL_CACHE.put(key, model);
        return model;
    }

    @Unique private static ModelResourceLocation sorti_getId(String namespace, String path) {
        String key = namespace + ":" + path;
        if (ID_CACHE.containsKey(key)) return ID_CACHE.get(key);

        ID_CACHE.put(key, new ModelResourceLocation(Identifier.of(namespace, path), "inventory"));
        return ID_CACHE.get(key);
    }
}
