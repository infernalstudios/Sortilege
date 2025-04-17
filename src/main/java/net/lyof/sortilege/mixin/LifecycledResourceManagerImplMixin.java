package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ModConfig;
import net.lyof.sortilege.setup.datagen.config.ConfiguredData;
import net.lyof.sortilege.setup.datagen.config.ConfiguredDataResourcePack;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Mixin(LifecycledResourceManagerImpl.class)
public class LifecycledResourceManagerImplMixin {
    @Unique
    private static Resource readAndApply(Optional<Resource> resource, ConfiguredData data) {
        Sortilege.log("Applying configured data: " + data.target);

        String result = "";
        if (resource.isEmpty())
            result = data.apply(null);
        else {
            try {
                result = data.apply(new String(resource.get().getInputStream().readAllBytes()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String finalResult = result;
        return new Resource(ConfiguredDataResourcePack.INSTANCE,
                () -> new CharSequenceInputStream(finalResult, Charset.defaultCharset()));
    }

    @Unique
    private static Resource readAndApply(Resource resource, ConfiguredData data) {
        if (resource.getPack() instanceof ConfiguredDataResourcePack) return resource;
        return readAndApply(Optional.of(resource), data);
    }

    @ModifyReturnValue(method = "getResource", at = @At("RETURN"))
    public Optional<Resource> getConfiguredResource(Optional<Resource> original, Identifier id) {
        ConfiguredData data = ConfiguredData.get(id);
        if (data == null || !data.enabled.get() || (original.isPresent() && original.get().getPack() instanceof ConfiguredDataResourcePack))
            return original;

        return Optional.of(readAndApply(original, data));
    }

    @ModifyReturnValue(method = "getAllResources", at = @At("RETURN"))
    public List<Resource> getAllConfiguredResource(List<Resource> original, Identifier id) {
        ConfiguredData data = ConfiguredData.get(id);
        if (data == null || !data.enabled.get()) return original;

        return original.stream()
                .map(resource -> readAndApply(resource, data)).toList();
    }

    @ModifyReturnValue(method = "findResources", at = @At("RETURN"))
    public Map<Identifier, Resource> findConfiguredResources(Map<Identifier, Resource> original,
                                                             String startingPath, Predicate<Identifier> allowedPathPredicate) {

        for (ConfiguredData data : ConfiguredData.INSTANCES) {
            if (data.enabled.get() && data.target.getPath().startsWith(startingPath + "/") && allowedPathPredicate.test(data.target)) {
                if (!original.containsKey(data.target)) {
                    original.put(data.target, readAndApply(Optional.empty(), data));
                }
            }
        }

        List<Identifier> ids = original.keySet().stream().toList();
        for (Identifier id : ids) {
            ConfiguredData data = ConfiguredData.get(id);
            if (data == null || !data.enabled.get()) continue;

            original.replace(id, readAndApply(original.get(id), data));
        }
        return original;
    }

    @ModifyReturnValue(method = "findAllResources", at = @At("RETURN"))
    public Map<Identifier, List<Resource>> findAllConfiguredResources(Map<Identifier, List<Resource>> original,
                                                                      String startingPath, Predicate<Identifier> allowedPathPredicate) {

        for (ConfiguredData data : ConfiguredData.INSTANCES) {
            if (data.enabled.get() && data.target.getPath().startsWith(startingPath) && allowedPathPredicate.test(data.target)) {
                if (!original.containsKey(data.target)) {
                    original.put(data.target, List.of(readAndApply(Optional.empty(), data)));
                }
            }
        }

        List<Identifier> ids = original.keySet().stream().toList();
        for (Identifier id : ids) {
            ConfiguredData data = ConfiguredData.get(id);
            if (data == null || !data.enabled.get()) continue;

            original.replace(id, original.get(id).stream()
                    .map(resource -> readAndApply(resource, data)).toList());
        }
        return original;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void reloadConfigs(ResourceType type, List<ResourcePack> packs, CallbackInfo ci) {
        ModConfig.register();
    }
}
