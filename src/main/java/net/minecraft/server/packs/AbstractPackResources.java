package net.minecraft.server.packs;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

public abstract class AbstractPackResources implements PackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String name;

    protected AbstractPackResources(String param0) {
        this.name = param0;
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> param0) throws IOException {
        IoSupplier<InputStream> var0 = this.getRootResource(new String[]{"pack.mcmeta"});
        if (var0 == null) {
            return null;
        } else {
            Object var4;
            try (InputStream var1 = var0.get()) {
                var4 = getMetadataFromStream(param0, var1);
            }

            return (T)var4;
        }
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
    public String packId() {
        return this.name;
    }
}
