package net.lyof.sortilege.setup;

import net.minecraft.resource.ResourceManager;

public interface EarlyReloadListener {
    void preload(ResourceManager manager);
}
