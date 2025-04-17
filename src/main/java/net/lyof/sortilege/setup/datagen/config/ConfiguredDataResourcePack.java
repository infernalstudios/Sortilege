package net.lyof.sortilege.setup.datagen.config;

import net.lyof.sortilege.Sortilege;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Set;

public class ConfiguredDataResourcePack implements ResourcePack {
    public static final ConfiguredDataResourcePack INSTANCE = new ConfiguredDataResourcePack();

    @Override
    public @Nullable InputSupplier<InputStream> openRoot(String... segments) {
        return null;
    }

    @Override
    public @Nullable InputSupplier<InputStream> open(ResourceType type, Identifier id) {
        return null;
    }

    @Override
    public void findResources(ResourceType type, String namespace, String prefix, ResultConsumer consumer) {

    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        return Set.of();
    }

    @Override
    public @Nullable <T> T parseMetadata(ResourceMetadataReader<T> metaReader) {
        return null;
    }

    @Override
    public String getName() {
        return Sortilege.MOD_ID + "_configured_data";
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isAlwaysStable() {
        return true;
    }
}
