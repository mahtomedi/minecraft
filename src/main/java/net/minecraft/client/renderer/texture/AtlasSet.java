package net.minecraft.client.renderer.texture;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AtlasSet implements AutoCloseable {
    private final Map<ResourceLocation, TextureAtlas> atlases;

    public AtlasSet(Collection<TextureAtlas> param0) {
        this.atlases = param0.stream().collect(Collectors.toMap(TextureAtlas::location, Function.identity()));
    }

    public TextureAtlas getAtlas(ResourceLocation param0) {
        return this.atlases.get(param0);
    }

    public TextureAtlasSprite getSprite(Material param0) {
        return this.atlases.get(param0.atlasLocation()).getSprite(param0.texture());
    }

    @Override
    public void close() {
        this.atlases.values().forEach(TextureAtlas::clearTextureData);
        this.atlases.clear();
    }
}
