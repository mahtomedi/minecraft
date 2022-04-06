package net.minecraft.server.packs.resources;

import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
}
