package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ResourceManager {
    @OnlyIn(Dist.CLIENT)
    Set<String> getNamespaces();

    Resource getResource(ResourceLocation var1) throws IOException;

    @OnlyIn(Dist.CLIENT)
    boolean hasResource(ResourceLocation var1);

    List<Resource> getResources(ResourceLocation var1) throws IOException;

    Collection<ResourceLocation> listResources(ResourceLocation var1, Predicate<String> var2);

    Collection<ResourceLocation> listResources(String var1, Predicate<String> var2);

    @OnlyIn(Dist.CLIENT)
    Stream<PackResources> listPacks();

    public static enum Empty implements ResourceManager {
        INSTANCE;

        @OnlyIn(Dist.CLIENT)
        @Override
        public Set<String> getNamespaces() {
            return ImmutableSet.of();
        }

        @Override
        public Resource getResource(ResourceLocation param0) throws IOException {
            throw new FileNotFoundException(param0.toString());
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public boolean hasResource(ResourceLocation param0) {
            return false;
        }

        @Override
        public List<Resource> getResources(ResourceLocation param0) {
            return ImmutableList.of();
        }

        @Override
        public Collection<ResourceLocation> listResources(ResourceLocation param0, Predicate<String> param1) {
            return ImmutableSet.of();
        }

        @Override
        public Collection<ResourceLocation> listResources(String param0, Predicate<String> param1) {
            return ImmutableSet.of();
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public Stream<PackResources> listPacks() {
            return Stream.of();
        }
    }
}
