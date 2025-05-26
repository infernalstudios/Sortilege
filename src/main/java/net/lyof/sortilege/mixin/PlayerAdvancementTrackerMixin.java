package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.Sortilege;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {
    @Shadow private ServerPlayerEntity owner;
    @Unique
    private static final Identifier GET_WOODEN_STAFF = Sortilege.makeID("get_wooden_staff");

    @WrapOperation(method = "grantCriterion", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/Advancement;toHoverableText()Lnet/minecraft/text/Text;"))
    public Text formatGetWoodenStaffAdvancement(Advancement instance, Operation<Text> original) {
        if (instance.getDisplay().getTitle().getString().contains("{playerName}")) {
            Text text = Text.literal(instance.getDisplay().getTitle().getString().replace("{playerName}", this.owner.getDisplayName().getString()));
            Formatting formatting = instance.getDisplay().getFrame().getTitleFormat();
            Text text2 = Texts.setStyleIfAbsent(text.copy(), Style.EMPTY.withColor(formatting)).append("\n").append(instance.getDisplay().getDescription());
            Text text3 = text.copy().styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, text2)));
            return Texts.bracketed(text3).formatted(formatting);
        }
        return original.call(instance);
    }
}
