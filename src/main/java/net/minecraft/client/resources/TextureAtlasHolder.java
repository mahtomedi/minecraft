package net.minecraft.client.resources;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class TextureAtlasHolder implements PreparableReloadListener, AutoCloseable {
    private final TextureAtlas textureAtlas;
    private final ResourceLocation atlasInfoLocation;
    private final Set<MetadataSectionSerializer<?>> metadataSections;

    public TextureAtlasHolder(TextureManager param0, ResourceLocation param1, ResourceLocation param2) {
        this(param0, param1, param2, SpriteLoader.DEFAULT_METADATA_SECTIONS);
    }

    public TextureAtlasHolder(TextureManager param0, ResourceLocation param1, ResourceLocation param2, Set<MetadataSectionSerializer<?>> param3) {
        this.atlasInfoLocation = param2;
        this.textureAtlas = new TextureAtlas(param1);
        param0.register(this.textureAtlas.location(), this.textureAtlas);
        this.metadataSections = param3;
    }

    protected TextureAtlasSprite getSprite(ResourceLocation param0) {
        return this.textureAtlas.getSprite(param0);
    }

    @Override
    public final CompletableFuture<Void> reload(
        PreparableReloadListener.PreparationBarrier param0,
        ResourceManager param1,
        ProfilerFiller param2,
        ProfilerFiller param3,
        Executor param4,
        Executor param5
    ) {
        return SpriteLoader.create(this.textureAtlas)
            .loadAndStitch(param1, this.atlasInfoLocation, 0, param4, this.metadataSections)
            .thenCompose(SpriteLoader.Preparations::waitForUpload)
            .thenCompose(param0::wait)
            .thenAcceptAsync(param1x -> this.apply(param1x, param3), param5);
    }

    private void apply(SpriteLoader.Preparations param0, ProfilerFiller param1) {
        param1.startTick();
        param1.push("upload");
        this.textureAtlas.upload(param0);
        param1.pop();
        param1.endTick();
    }

    @Override
    public void close() {
        this.textureAtlas.clearTextureData();
    }
}
