package net.lyof.sortilege.screen;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.screen.custom.KnowledgeBookScreenHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;

public class ModScreenHandlers {
    public static void register() {}

    public static final MenuType<KnowledgeBookScreenHandler> KNOWLEDGE_BOOK =
            Registry.register(BuiltInRegistries.MENU, Sortilege.MOD.makeID("knowledge_book"),
                    new MenuType<>(KnowledgeBookScreenHandler.decode, FeatureFlagSet.of()));
}
