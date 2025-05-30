package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.custom.potion.AntidotePotionItem;
import net.lyof.sortilege.item.custom.potion.CustomPotionData;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemModels.class)
public class ItemModelsMixin {
    @Shadow @Final private BakedModelManager modelManager;

    @WrapMethod(method = "getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;")
    public BakedModel getCustomModel(ItemStack stack, Operation<BakedModel> original) {
        if (!(stack.getItem() instanceof PotionItem) || stack.getItem() instanceof AntidotePotionItem) return original.call(stack);

        Identifier potion = Registries.POTION.getId(PotionUtil.getPotion(stack));

        String path = "potions/";
        if (stack.isOf(Items.SPLASH_POTION)) path += "splash/";
        else if (stack.isOf(Items.LINGERING_POTION)) path += "lingering/";

        BakedModel model = this.modelManager.getModel(new ModelIdentifier(Identifier.of(potion.getNamespace(),
                path + potion.getPath()), "inventory"));
        if (model == this.modelManager.getMissingModel())
            return original.call(stack);
        return model;
    }
}
