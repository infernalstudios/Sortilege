package net.lyof.sortilege.setup;

import net.minecraft.server.packs.resources.ResourceManager;

public interface EarlyReloadListener {
    void preload(ResourceManager manager);
}
