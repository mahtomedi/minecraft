package net.minecraft.client.resources;

import java.util.stream.Stream;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class TextureAtlasHolder extends SimplePreparableReloadListener<TextureAtlas.Preparations> implements AutoCloseable {
    private final TextureAtlas textureAtlas;
    private final String prefix;

    public TextureAtlasHolder(TextureManager param0, ResourceLocation param1, String param2) {
        this.prefix = param2;
        this.textureAtlas = new TextureAtlas(param1);
        param0.register(this.textureAtlas.location(), this.textureAtlas);
    }

    protected abstract Stream<ResourceLocation> getResourcesToLoad();

    protected TextureAtlasSprite getSprite(ResourceLocation param0) {
        return this.textureAtlas.getSprite(this.resolveLocation(param0));
    }

    private ResourceLocation resolveLocation(ResourceLocation param0) {
        return new ResourceLocation(param0.getNamespace(), this.prefix + "/" + param0.getPath());
    }

    protected TextureAtlas.Preparations prepare(ResourceManager param0, ProfilerFiller param1) {
        param1.startTick();
        param1.push("stitching");
        TextureAtlas.Preparations var0 = this.textureAtlas.prepareToStitch(param0, this.getResourcesToLoad().map(this::resolveLocation), param1, 0);
        param1.pop();
        param1.endTick();
        return var0;
    }

    protected void apply(TextureAtlas.Preparations param0, ResourceManager param1, ProfilerFiller param2) {
        param2.startTick();
        param2.push("upload");
        this.textureAtlas.reload(param0);
        param2.pop();
        param2.endTick();
    }

    @Override
    public void close() {
        this.textureAtlas.clearTextureData();
    }
}
