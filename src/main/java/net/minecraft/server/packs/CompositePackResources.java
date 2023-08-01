package net.minecraft.server.packs;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;

public class CompositePackResources implements PackResources {
    private final PackResources primaryPackResources;
    private final List<PackResources> packResourcesStack;

    public CompositePackResources(PackResources param0, List<PackResources> param1) {
        this.primaryPackResources = param0;
        List<PackResources> var0 = new ArrayList<>(param1.size() + 1);
        var0.addAll(Lists.reverse(param1));
        var0.add(param0);
        this.packResourcesStack = List.copyOf(var0);
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... param0) {
        return this.primaryPackResources.getRootResource(param0);
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType param0, ResourceLocation param1) {
        for(PackResources var0 : this.packResourcesStack) {
            IoSupplier<InputStream> var1 = var0.getResource(param0, param1);
            if (var1 != null) {
                return var1;
            }
        }

        return null;
    }

    @Override
    public void listResources(PackType param0, String param1, String param2, PackResources.ResourceOutput param3) {
        Map<ResourceLocation, IoSupplier<InputStream>> var0 = new HashMap<>();

        for(PackResources var1 : this.packResourcesStack) {
            var1.listResources(param0, param1, param2, var0::putIfAbsent);
        }

        var0.forEach(param3);
    }

    @Override
    public Set<String> getNamespaces(PackType param0) {
        Set<String> var0 = new HashSet<>();

        for(PackResources var1 : this.packResourcesStack) {
            var0.addAll(var1.getNamespaces(param0));
        }

        return var0;
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> param0) throws IOException {
        return this.primaryPackResources.getMetadataSection(param0);
    }

    @Override
    public String packId() {
        return this.primaryPackResources.packId();
    }

    @Override
    public boolean isBuiltin() {
        return this.primaryPackResources.isBuiltin();
    }

    @Override
    public void close() {
        this.packResourcesStack.forEach(PackResources::close);
    }
}
