package net.minecraft.server.packs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractResourcePack implements Pack {
    private static final Logger LOGGER = LogManager.getLogger();
    protected final File file;

    public AbstractResourcePack(File param0) {
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

    @OnlyIn(Dist.CLIENT)
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
        Object var4;
        try (InputStream var0 = this.getResource("pack.mcmeta")) {
            var4 = getMetadataFromStream(param0, var0);
        }

        return (T)var4;
    }

    @Nullable
    public static <T> T getMetadataFromStream(MetadataSectionSerializer<T> param0, InputStream param1) {
        JsonObject var1;
        try (BufferedReader var0 = new BufferedReader(new InputStreamReader(param1, StandardCharsets.UTF_8))) {
            var1 = GsonHelper.parse(var0);
        } catch (JsonParseException | IOException var18) {
            LOGGER.error("Couldn't load {} metadata", param0.getMetadataSectionName(), var18);
            return null;
        }

        if (!var1.has(param0.getMetadataSectionName())) {
            return null;
        } else {
            try {
                return param0.fromJson(GsonHelper.getAsJsonObject(var1, param0.getMetadataSectionName()));
            } catch (JsonParseException var15) {
                LOGGER.error("Couldn't load {} metadata", param0.getMetadataSectionName(), var15);
                return null;
            }
        }
    }

    @Override
    public String getName() {
        return this.file.getName();
    }
}
