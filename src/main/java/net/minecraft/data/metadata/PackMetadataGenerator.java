package net.minecraft.data.metadata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.bridge.game.PackType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.DetectedVersion;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.flag.FeatureFlagSet;

public class PackMetadataGenerator implements DataProvider {
    private final String name;
    private final PackOutput output;
    private final Map<String, Supplier<JsonElement>> elements = new HashMap<>();

    public PackMetadataGenerator(PackOutput param0, String param1) {
        this.output = param0;
        this.name = param1;
    }

    public <T> PackMetadataGenerator add(MetadataSectionType<T> param0, T param1) {
        this.elements.put(param0.getMetadataSectionName(), () -> param0.toJson(param1));
        return this;
    }

    @Override
    public void run(CachedOutput param0) throws IOException {
        JsonObject var0 = new JsonObject();
        this.elements.forEach((param1, param2) -> var0.add(param1, param2.get()));
        DataProvider.saveStable(param0, var0, this.output.getOutputFolder().resolve("pack.mcmeta"));
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static PackMetadataGenerator forFeaturePack(PackOutput param0, String param1, Component param2, FeatureFlagSet param3) {
        return new PackMetadataGenerator(param0, "Pack metadata for " + param1)
            .add(PackMetadataSection.TYPE, new PackMetadataSection(param2, DetectedVersion.BUILT_IN.getPackVersion(PackType.DATA)))
            .add(FeatureFlagsMetadataSection.TYPE, new FeatureFlagsMetadataSection(param3));
    }
}
