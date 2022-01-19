package net.minecraft.server.packs;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

public abstract class AbstractPackResources implements PackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final File file;

    public AbstractPackResources(File param0) {
        this.file = param0;
    }

    private static String getPathFromLocation(PackType param0, ResourceLocation param1) {
        return String.format("%s/%s/%s", param0.getDirectory(), param1.getNamespace(), param1.getPath());
    }

    protected static String getRelativePath(File param0, File param1) {
        return param0.toURI().relativize(param1.toURI()).getPath();
    }

    @Override
    public InputStream getResource(PackType param0, ResourceLocation param1) throws IOException {
        return this.getResource(getPathFromLocation(param0, param1));
    }

    @Override
    public boolean hasResource(PackType param0, ResourceLocation param1) {
        return this.hasResource(getPathFromLocation(param0, param1));
    }

    protected abstract InputStream getResource(String var1) throws IOException;

    @Override
    public InputStream getRootResource(String param0) throws IOException {
        if (!param0.contains("/") && !param0.contains("\\")) {
            return this.getResource(param0);
        } else {
            throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
        }
    }

    protected abstract boolean hasResource(String var1);

    protected void logWarning(String param0) {
        LOGGER.warn("ResourcePack: ignored non-lowercase namespace: {} in {}", param0, this.file);
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> param0) throws IOException {
        Object var3;
        try (InputStream var0 = this.getResource("pack.mcmeta")) {
            var3 = getMetadataFromStream(param0, var0);
        }

        return (T)var3;
    }

    @Nullable
    public static <T> T getMetadataFromStream(MetadataSectionSerializer<T> param0, InputStream param1) {
        JsonObject var1;
        try (BufferedReader var0 = new BufferedReader(new InputStreamReader(param1, StandardCharsets.UTF_8))) {
            var1 = GsonHelper.parse(var0);
        } catch (Exception var9) {
            LOGGER.error("Couldn't load {} metadata", param0.getMetadataSectionName(), var9);
            return null;
        }

        if (!var1.has(param0.getMetadataSectionName())) {
            return null;
        } else {
            try {
                return param0.fromJson(GsonHelper.getAsJsonObject(var1, param0.getMetadataSectionName()));
            } catch (Exception var7) {
                LOGGER.error("Couldn't load {} metadata", param0.getMetadataSectionName(), var7);
                return null;
            }
        }
    }

    @Override
    public String getName() {
        return this.file.getName();
    }
}
