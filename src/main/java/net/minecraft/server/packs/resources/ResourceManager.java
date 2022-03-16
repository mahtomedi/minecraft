package net.minecraft.server.packs.resources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;

public interface ResourceManager extends ResourceProvider {
    Set<String> getNamespaces();

    boolean hasResource(ResourceLocation var1);

    List<ResourceThunk> getResourceStack(ResourceLocation var1) throws IOException;

    Map<ResourceLocation, ResourceThunk> listResources(String var1, Predicate<ResourceLocation> var2);

    Map<ResourceLocation, List<ResourceThunk>> listResourceStacks(String var1, Predicate<ResourceLocation> var2);

    Stream<PackResources> listPacks();

    public static enum Empty implements ResourceManager {
        INSTANCE;

        @Override
        public Set<String> getNamespaces() {
            return Set.of();
        }

        @Override
        public Resource getResource(ResourceLocation param0) throws IOException {
            throw new FileNotFoundException(param0.toString());
        }

        @Override
        public boolean hasResource(ResourceLocation param0) {
            return false;
        }

        @Override
        public List<ResourceThunk> getResourceStack(ResourceLocation param0) throws IOException {
            throw new FileNotFoundException(param0.toString());
        }

        @Override
        public Map<ResourceLocation, ResourceThunk> listResources(String param0, Predicate<ResourceLocation> param1) {
            return Map.of();
        }

        @Override
        public Map<ResourceLocation, List<ResourceThunk>> listResourceStacks(String param0, Predicate<ResourceLocation> param1) {
            return Map.of();
        }

        @Override
        public Stream<PackResources> listPacks() {
            return Stream.of();
        }
    }
}
