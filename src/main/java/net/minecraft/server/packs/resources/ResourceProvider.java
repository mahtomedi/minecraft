package net.minecraft.server.packs.resources;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface ResourceProvider {
    Optional<Resource> getResource(ResourceLocation var1);

    default Resource getResourceOrThrow(ResourceLocation param0) throws FileNotFoundException {
        return this.getResource(param0).orElseThrow(() -> new FileNotFoundException(param0.toString()));
    }

    default InputStream open(ResourceLocation param0) throws IOException {
        return this.getResourceOrThrow(param0).open();
    }

    default BufferedReader openAsReader(ResourceLocation param0) throws IOException {
        return this.getResourceOrThrow(param0).openAsReader();
    }

    static ResourceProvider fromMap(Map<ResourceLocation, Resource> param0) {
        return param1 -> Optional.ofNullable(param0.get(param1));
    }
}
