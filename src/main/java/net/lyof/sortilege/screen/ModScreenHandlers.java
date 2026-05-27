package net.lyof.sortilege.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.screen.custom.KnowledgeBookScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;

public class ModScreenHandlers {
    public static void register() {}

    public static final ExtendedScreenHandlerType<KnowledgeBookScreenHandler> KNOWLEDGE_BOOK =
            Registry.register(Registries.SCREEN_HANDLER, Sortilege.makeID("knowledge_book"),
                    new ExtendedScreenHandlerType<>(KnowledgeBookScreenHandler::new));
}
