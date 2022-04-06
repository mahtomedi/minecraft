package net.minecraft.server.packs.resources;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;

public interface ResourceManager extends ResourceProvider {
    Set<String> getNamespaces();

    List<Resource> getResourceStack(ResourceLocation var1);

    Map<ResourceLocation, Resource> listResources(String var1, Predicate<ResourceLocation> var2);

    Map<ResourceLocation, List<Resource>> listResourceStacks(String var1, Predicate<ResourceLocation> var2);

    Stream<PackResources> listPacks();

    public static enum Empty implements ResourceManager {
        INSTANCE;

        @Override
        public Set<String> getNamespaces() {
            return Set.of();
        }

        @Override
        public Optional<Resource> getResource(ResourceLocation param0) {
            return Optional.empty();
        }

        @Override
        public List<Resource> getResourceStack(ResourceLocation param0) {
            return List.of();
        }

        @Override
        public Map<ResourceLocation, Resource> listResources(String param0, Predicate<ResourceLocation> param1) {
            return Map.of();
        }

        @Override
        public Map<ResourceLocation, List<Resource>> listResourceStacks(String param0, Predicate<ResourceLocation> param1) {
            return Map.of();
        }

        @Override
        public Stream<PackResources> listPacks() {
            return Stream.of();
        }
    }
}
