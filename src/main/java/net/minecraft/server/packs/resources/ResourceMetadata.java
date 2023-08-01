package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;

public interface ResourceMetadata {
    ResourceMetadata EMPTY = new ResourceMetadata() {
        @Override
        public <T> Optional<T> getSection(MetadataSectionSerializer<T> param0) {
            return Optional.empty();
        }
    };
    IoSupplier<ResourceMetadata> EMPTY_SUPPLIER = () -> EMPTY;

    static ResourceMetadata fromJsonStream(InputStream param0) throws IOException {
        ResourceMetadata var3;
        try (BufferedReader var0 = new BufferedReader(new InputStreamReader(param0, StandardCharsets.UTF_8))) {
            final JsonObject var1 = GsonHelper.parse(var0);
            var3 = new ResourceMetadata() {
                @Override
                public <T> Optional<T> getSection(MetadataSectionSerializer<T> param0) {
                    String var0 = param0.getMetadataSectionName();
                    return var1.has(var0) ? Optional.of(param0.fromJson(GsonHelper.getAsJsonObject(var1, var0))) : Optional.empty();
                }
            };
        }

        return var3;
    }

    <T> Optional<T> getSection(MetadataSectionSerializer<T> var1);

    default ResourceMetadata copySections(Collection<MetadataSectionSerializer<?>> param0) {
        ResourceMetadata.Builder var0 = new ResourceMetadata.Builder();

        for(MetadataSectionSerializer<?> var1 : param0) {
            this.copySection(var0, var1);
        }

        return var0.build();
    }

    private <T> void copySection(ResourceMetadata.Builder param0, MetadataSectionSerializer<T> param1) {
        this.getSection(param1).ifPresent(param2 -> param0.put(param1, param2));
    }

    public static class Builder {
        private final ImmutableMap.Builder<MetadataSectionSerializer<?>, Object> map = ImmutableMap.builder();

        public <T> ResourceMetadata.Builder put(MetadataSectionSerializer<T> param0, T param1) {
            this.map.put(param0, param1);
            return this;
        }

        public ResourceMetadata build() {
            final ImmutableMap<MetadataSectionSerializer<?>, Object> var0 = this.map.build();
            return var0.isEmpty() ? ResourceMetadata.EMPTY : new ResourceMetadata() {
                @Override
                public <T> Optional<T> getSection(MetadataSectionSerializer<T> param0) {
                    return Optional.ofNullable((T)var0.get(param0));
                }
            };
        }
    }
}
