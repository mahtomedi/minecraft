package net.minecraft.data.metadata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.DetectedVersion;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.flag.FeatureFlagSet;

public class PackMetadataGenerator implements DataProvider {
    private final PackOutput output;
    private final Map<String, Supplier<JsonElement>> elements = new HashMap<>();

    public PackMetadataGenerator(PackOutput param0) {
        this.output = param0;
    }

    public <T> PackMetadataGenerator add(MetadataSectionType<T> param0, T param1) {
        this.elements.put(param0.getMetadataSectionName(), () -> param0.toJson(param1));
        return this;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput param0) {
        JsonObject var0 = new JsonObject();
        this.elements.forEach((param1, param2) -> var0.add(param1, param2.get()));
        return DataProvider.saveStable(param0, var0, this.output.getOutputFolder().resolve("pack.mcmeta"));
    }

    @Override
    public final String getName() {
        return "Pack Metadata";
    }

    public static PackMetadataGenerator forFeaturePack(PackOutput param0, Component param1) {
        return new PackMetadataGenerator(param0)
            .add(PackMetadataSection.TYPE, new PackMetadataSection(param1, DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA), Optional.empty()));
    }

    public static PackMetadataGenerator forFeaturePack(PackOutput param0, Component param1, FeatureFlagSet param2) {
        return forFeaturePack(param0, param1).add(FeatureFlagsMetadataSection.TYPE, new FeatureFlagsMetadataSection(param2));
    }
}
