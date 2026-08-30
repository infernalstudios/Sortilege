package net.lyof.sortilege.mixin;

import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {
/* TODO: AdvancementType.createAnnouncement

    @WrapOperation(method = "award", at = @At(value = "INVOKE", target = ""))
    public Component formatGetWoodenStaffAdvancement(Advancement instance, Operation<Component> original) {
        if (instance.getDisplay().getTitle().getString().contains("{playerName}")) {
            Component text = Component.literal(instance.getDisplay().getTitle().getString().replace("{playerName}", this.player.getDisplayName().getString()));
            ChatFormatting formatting = instance.getDisplay().getFrame().getChatColor();
            Component text2 = ComponentUtils.mergeStyles(text.copy(), Style.EMPTY.withColor(formatting)).append("\n").append(instance.getDisplay().getDescription());
            Component text3 = text.copy().withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, text2)));
            return ComponentUtils.wrapInSquareBrackets(text3).withStyle(formatting);
        }
        return original.call(instance);
    }*/
}
