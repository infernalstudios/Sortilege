package net.lyof.sortilege.setup.datagen.config;

import net.lyof.sortilege.Sortilege;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Set;

public class ConfiguredDataResourcePack implements PackResources {
    public static final ConfiguredDataResourcePack INSTANCE = new ConfiguredDataResourcePack();

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... segments) {
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, ResourceLocation id) {
        return null;
    }

    @Override
    public void listResources(PackType type, String namespace, String prefix, ResourceOutput consumer) {

    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return Set.of();
    }

    @Override
    public @Nullable <T> T getMetadataSection(MetadataSectionSerializer<T> metaReader) {
        return null;
    }

    @Override
    public String packId() {
        return Sortilege.MOD_ID + "_configured_data";
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isBuiltin() {
        return true;
    }
}
