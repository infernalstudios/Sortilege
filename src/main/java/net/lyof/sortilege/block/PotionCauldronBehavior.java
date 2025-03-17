package net.lyof.sortilege.block;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.Map;

public class PotionCauldronBehavior {
    public static final Map<Item, CauldronBehavior> POTION_CAULDRON_BEHAVIOR = new HashMap<>() {
        @Override
        public CauldronBehavior get(Object key) {
            if (!this.containsKey(key)) return (state, world, pos, player, hand, stack) -> ActionResult.PASS;
            return super.get(key);
        }
    };

    public static void register() {
        
    }
}
