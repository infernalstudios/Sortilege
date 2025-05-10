package net.lyof.sortilege.setup.asm;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class EarlyRiser implements Runnable {
    @Override
    public void run() {
        MappingResolver remapper = FabricLoader.getInstance().getMappingResolver();
        String target = remapper.mapClassName("intermediary", "net.minecraft.class_1886");
        ClassTinkerers.enumBuilder(target)
                .addEnumSubclass("Sortilege$STAFF", "net.lyof.sortilege.setup.asm.StaffEnchantmentTarget")
                .build();
    }
}
