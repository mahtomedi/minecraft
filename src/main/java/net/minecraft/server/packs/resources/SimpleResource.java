package net.minecraft.server.packs.resources;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleResource implements Resource {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Executor IO_EXECUTOR = Executors.newSingleThreadExecutor(
        new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("Resource IO {0}")
            .setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER))
            .build()
    );
    private final String sourceName;
    private final ResourceLocation location;
    private final InputStream resourceStream;
    private final InputStream metadataStream;
    @OnlyIn(Dist.CLIENT)
    private boolean triedMetadata;
    @OnlyIn(Dist.CLIENT)
    private JsonObject metadata;

    public SimpleResource(String param0, ResourceLocation param1, InputStream param2, @Nullable InputStream param3) {
        this.sourceName = param0;
        this.location = param1;
        this.resourceStream = param2;
        this.metadataStream = param3;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ResourceLocation getLocation() {
        return this.location;
    }

    @Override
    public InputStream getInputStream() {
        return this.resourceStream;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean hasMetadata() {
        return this.metadataStream != null;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    @Override
    public <T> T getMetadata(MetadataSectionSerializer<T> param0) {
        if (!this.hasMetadata()) {
            return null;
        } else {
            if (this.metadata == null && !this.triedMetadata) {
                this.triedMetadata = true;
                BufferedReader var0 = null;

                try {
                    var0 = new BufferedReader(new InputStreamReader(this.metadataStream, StandardCharsets.UTF_8));
                    this.metadata = GsonHelper.parse(var0);
                } finally {
                    IOUtils.closeQuietly((Reader)var0);
                }
            }

            if (this.metadata == null) {
                return null;
            } else {
                String var1 = param0.getMetadataSectionName();
                return this.metadata.has(var1) ? param0.fromJson(GsonHelper.getAsJsonObject(this.metadata, var1)) : null;
            }
        }
    }

    @Override
    public String getSourceName() {
        return this.sourceName;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof SimpleResource)) {
            return false;
        } else {
            SimpleResource var0 = (SimpleResource)param0;
            if (this.location != null) {
                if (!this.location.equals(var0.location)) {
                    return false;
                }
            } else if (var0.location != null) {
                return false;
            }

            if (this.sourceName != null) {
                if (!this.sourceName.equals(var0.sourceName)) {
                    return false;
                }
            } else if (var0.sourceName != null) {
                return false;
            }

            return true;
        }
    }

    @Override
    public int hashCode() {
        int var0 = this.sourceName != null ? this.sourceName.hashCode() : 0;
        return 31 * var0 + (this.location != null ? this.location.hashCode() : 0);
    }

    @Override
    public void close() throws IOException {
        this.resourceStream.close();
        if (this.metadataStream != null) {
            this.metadataStream.close();
        }

    }
}
