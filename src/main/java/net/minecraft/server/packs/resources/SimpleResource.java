package net.minecraft.server.packs.resources;

import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;

public class SimpleResource implements Resource {
    private final String sourceName;
    private final ResourceLocation location;
    private final InputStream resourceStream;
    @Nullable
    private InputStream metadataStream;
    @Nullable
    private JsonObject metadata;

    public SimpleResource(String param0, ResourceLocation param1, InputStream param2, @Nullable InputStream param3) {
        this.sourceName = param0;
        this.location = param1;
        this.resourceStream = param2;
        this.metadataStream = param3;
    }

    @Override
    public ResourceLocation getLocation() {
        return this.location;
    }

    @Override
    public InputStream getInputStream() {
        return this.resourceStream;
    }

    @Override
    public boolean hasMetadata() {
        return this.metadata != null || this.metadataStream != null;
    }

    @Nullable
    @Override
    public <T> T getMetadata(MetadataSectionSerializer<T> param0) {
        if (this.metadata == null && this.metadataStream != null) {
            BufferedReader var0 = null;

            try {
                var0 = new BufferedReader(new InputStreamReader(this.metadataStream, StandardCharsets.UTF_8));
                this.metadata = GsonHelper.parse(var0);
            } finally {
                IOUtils.closeQuietly((Reader)var0);
            }

            this.metadataStream = null;
        }

        if (this.metadata == null) {
            return null;
        } else {
            String var1 = param0.getMetadataSectionName();
            return this.metadata.has(var1) ? param0.fromJson(GsonHelper.getAsJsonObject(this.metadata, var1)) : null;
        }
    }

    @Override
    public String getSourceName() {
        return this.sourceName;
    }

    @Override
    public void close() throws IOException {
        this.resourceStream.close();
        if (this.metadataStream != null) {
            this.metadataStream.close();
        }

    }
}
