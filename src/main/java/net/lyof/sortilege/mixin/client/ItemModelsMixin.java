package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.lyof.sortilege.item.custom.AntidotePotionItem;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

@Mixin(ItemModels.class)
public class ItemModelsMixin {
    @Unique private static final Map<String, BakedModel> CACHE = new HashMap<>();

    @Shadow @Final private BakedModelManager modelManager;

    @WrapMethod(method = "getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;")
    public BakedModel getCustomModel(ItemStack stack, Operation<BakedModel> original) {
        if (!(stack.getItem() instanceof PotionItem) || stack.getItem() instanceof AntidotePotionItem) return original.call(stack);
        if (!stack.hasNbt()) return original.call(stack);
        
        Identifier id = new Identifier(stack.getNbt().getString("Potion"));
        String key = stack.getItem().getClass().getName() + "@" + Integer.toHexString(stack.getItem().hashCode()) + "/" + id;
        if (CACHE.containsKey(key)) return CACHE.get(key);

        String base = "";
        if (stack.isOf(Items.SPLASH_POTION)) base = "splash/";
        else if (stack.isOf(Items.LINGERING_POTION)) base = "lingering/";

        BakedModel model = this.modelManager.getModel(new ModelIdentifier(Identifier.of(id.getNamespace(),
                "potions/" + base + id.getPath()), "inventory"));

        if (model == this.modelManager.getMissingModel()) {
            String type = "";
            if (id.getPath().startsWith("strong_"))
                type = "strong_";
            else if (id.getPath().startsWith("long_"))
                type = "long_";

            model = this.modelManager.getModel(new ModelIdentifier(Identifier.of("minecraft",
                    type + base.replace("/", "_") + "potion"), "inventory"));
        }

        if (model == this.modelManager.getMissingModel())
            model = original.call(stack);

        CACHE.put(key, model);
        return model;
    }
}
