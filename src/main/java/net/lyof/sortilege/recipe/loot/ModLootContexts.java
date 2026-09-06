package net.lyof.sortilege.recipe.loot;

import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.function.Consumer;

public class ModLootContexts {
    public static void register() {}

    private static LootContextParamSet register(Consumer<LootContextParamSet.Builder> consumer) {
        LootContextParamSet.Builder builder = new LootContextParamSet.Builder();
        consumer.accept(builder);
        return builder.build();
    }

    public static final LootContextParamSet STAFF_SHOOT = register(builder ->
            builder.required(LootContextParams.THIS_ENTITY)
                    .required(LootContextParams.ENCHANTMENT_LEVEL)
                    .required(LootContextParams.ORIGIN)
                    .required(LootContextParams.TOOL));
    public static final LootContextParamSet STAFF_DAMAGE = register(builder ->
            builder.required(LootContextParams.THIS_ENTITY)
                    .required(LootContextParams.ENCHANTMENT_LEVEL)
                    .required(LootContextParams.ORIGIN)
                    .required(LootContextParams.DAMAGE_SOURCE)
                    .required(LootContextParams.DIRECT_ATTACKING_ENTITY)
                    .required(LootContextParams.ATTACKING_ENTITY)
                    .required(LootContextParams.TOOL));
}
