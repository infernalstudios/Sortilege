package net.lyof.sortilege.utils;

import net.lyof.sortilege.configs.ModJsonConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.RegistryObject;

public class ItemHelper {
    public static int getEnchantLevel(RegistryObject<Enchantment> enchant, ItemStack item) {
        return getEnchantLevel(enchant.get(), item);
    }

    public static int getEnchantLevel(Enchantment enchant, ItemStack item) {
        return EnchantmentHelper.getTagEnchantmentLevel(enchant, item);
    }

    public static boolean hasEnchant(RegistryObject<Enchantment> enchant, ItemStack item) {
        return hasEnchant(enchant.get(), item);
    }

    public static boolean hasEnchant(Enchantment enchant, ItemStack item) {
        return getEnchantLevel(enchant, item) > 0;
    }


    public static final String ENCHLIMIT_PATH = "enchantments.enchant_limiter.";

    public static int getMaxEnchantValue(ItemStack itemstack) {
        String id = itemstack.getItem().getDescriptionId();
        id = id.substring(id.indexOf(".") + 1).replaceAll("\\.", ":");
        int default_limit = new ModJsonConfigs.ConfigEntry<>(ENCHLIMIT_PATH + "default", 3).get();
        boolean sum = new ModJsonConfigs.ConfigEntry<>
                (ENCHLIMIT_PATH + "override_mode", "absolute").get().equals("relative");

        int limit = new ModJsonConfigs.ConfigEntry<>(ENCHLIMIT_PATH + "overrides." + id, sum ? 0 : -1).get();


        if (sum)
            return limit + default_limit;
        return limit == -1 ? default_limit : limit;
    }

    public static Component getShiftTooltip() {
        return Component.translatable("tooltip.press_shift.left").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable("tooltip.press_shift.center").withStyle(ChatFormatting.GRAY))
                .append(Component.translatable("tooltip.press_shift.right").withStyle(ChatFormatting.DARK_GRAY));
    }
}
