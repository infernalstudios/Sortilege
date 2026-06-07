package net.lyof.sortilege.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.screen.custom.KnowledgeBookScreenHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModScreenHandlers {
    public static void register() {}

    public static final ExtendedScreenHandlerType<KnowledgeBookScreenHandler> KNOWLEDGE_BOOK =
            Registry.register(BuiltInRegistries.MENU, Sortilege.makeID("knowledge_book"),
                    new ExtendedScreenHandlerType<>(KnowledgeBookScreenHandler::new));
}
