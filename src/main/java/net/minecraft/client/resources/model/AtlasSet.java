package net.minecraft.client.resources.model;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AtlasSet implements AutoCloseable {
    private final Map<ResourceLocation, AtlasSet.AtlasEntry> atlases;

    public AtlasSet(Map<ResourceLocation, ResourceLocation> param0, TextureManager param1) {
        this.atlases = param0.entrySet().stream().collect(Collectors.toMap(Entry::getKey, param1x -> {
            TextureAtlas var0 = new TextureAtlas(param1x.getKey());
            param1.register(param1x.getKey(), var0);
            return new AtlasSet.AtlasEntry(var0, param1x.getValue());
        }));
    }

    public TextureAtlas getAtlas(ResourceLocation param0) {
        return this.atlases.get(param0).atlas();
    }

    @Override
    public void close() {
        this.atlases.values().forEach(AtlasSet.AtlasEntry::close);
        this.atlases.clear();
    }

    public Map<ResourceLocation, CompletableFuture<AtlasSet.StitchResult>> scheduleLoad(ResourceManager param0, int param1, Executor param2) {
        return this.atlases
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    Entry::getKey,
                    param3 -> {
                        AtlasSet.AtlasEntry var0 = param3.getValue();
                        return SpriteLoader.create(var0.atlas)
                            .loadAndStitch(param0, var0.atlasInfoLocation, param1, param2)
                            .thenApply(param1x -> new AtlasSet.StitchResult(var0.atlas, param1x));
                    }
                )
            );
    }

    @OnlyIn(Dist.CLIENT)
    static record AtlasEntry(TextureAtlas atlas, ResourceLocation atlasInfoLocation) implements AutoCloseable {
        @Override
        public void close() {
            this.atlas.clearTextureData();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class StitchResult {
        private final TextureAtlas atlas;
        private final SpriteLoader.Preparations preparations;

        public StitchResult(TextureAtlas param0, SpriteLoader.Preparations param1) {
            this.atlas = param0;
            this.preparations = param1;
        }

        @Nullable
        public TextureAtlasSprite getSprite(ResourceLocation param0) {
            return this.preparations.regions().get(param0);
        }

        public TextureAtlasSprite missing() {
            return this.preparations.missing();
        }

        public CompletableFuture<Void> readyForUpload() {
            return this.preparations.readyForUpload();
        }

        public void upload() {
            this.atlas.upload(this.preparations);
        }
    }
}
