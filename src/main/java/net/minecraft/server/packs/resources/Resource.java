package net.minecraft.server.packs.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;

public class Resource {
    private final String packId;
    private final Resource.IoSupplier<InputStream> streamSupplier;
    private final Resource.IoSupplier<ResourceMetadata> metadataSupplier;
    @Nullable
    private ResourceMetadata cachedMetadata;

    public Resource(String param0, Resource.IoSupplier<InputStream> param1, Resource.IoSupplier<ResourceMetadata> param2) {
        this.packId = param0;
        this.streamSupplier = param1;
        this.metadataSupplier = param2;
    }

    public Resource(String param0, Resource.IoSupplier<InputStream> param1) {
        this.packId = param0;
        this.streamSupplier = param1;
        this.metadataSupplier = () -> ResourceMetadata.EMPTY;
        this.cachedMetadata = ResourceMetadata.EMPTY;
    }

    public String sourcePackId() {
        return this.packId;
    }

    public InputStream open() throws IOException {
        return this.streamSupplier.get();
    }

    public BufferedReader openAsReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.open(), StandardCharsets.UTF_8));
    }

    public ResourceMetadata metadata() throws IOException {
        if (this.cachedMetadata == null) {
            this.cachedMetadata = this.metadataSupplier.get();
        }

        return this.cachedMetadata;
    }

    @FunctionalInterface
    public interface IoSupplier<T> {
        T get() throws IOException;
    }
}
