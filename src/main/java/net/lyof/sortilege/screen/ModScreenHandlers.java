package net.lyof.sortilege.screen;

import com.mojang.serialization.MapCodec;
import net.lcc.sollib.api.common.registry.SHolder;
import net.lcc.sollib.api.common.registry.SolModContainer;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.screen.custom.KnowledgeBookScreenHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public class ModScreenHandlers {
    public static class MenuHolder<T extends AbstractContainerMenu> extends SHolder<MenuType<T>> {
        public MenuHolder(SolModContainer mod, String name, Supplier<MenuType<T>> entrySupplier) {
            super(mod, name, entrySupplier);
        }

        @Override
        public Registry<MenuType<T>> getRegistry() {
            return (Registry<MenuType<T>>) (Object) BuiltInRegistries.MENU;
        }
    }


    public static void register() {}

    public static final MenuHolder<KnowledgeBookScreenHandler> KNOWLEDGE_BOOK =
            Sortilege.MOD.register(MenuHolder.class, "knowledge_book",
                    () -> new MenuType<>(KnowledgeBookScreenHandler.decode, FeatureFlagSet.of()));
}
