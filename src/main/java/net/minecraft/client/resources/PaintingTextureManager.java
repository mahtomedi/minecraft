package net.minecraft.client.resources;

import java.util.stream.Stream;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PaintingTextureManager extends TextureAtlasHolder {
    private static final ResourceLocation BACK_SPRITE_LOCATION = new ResourceLocation("back");

    public PaintingTextureManager(TextureManager param0) {
        super(param0, new ResourceLocation("textures/atlas/paintings.png"), "painting");
    }

    @Override
    protected Stream<ResourceLocation> getResourcesToLoad() {
        return Stream.concat(Registry.PAINTING_VARIANT.keySet().stream(), Stream.of(BACK_SPRITE_LOCATION));
    }

    public TextureAtlasSprite get(PaintingVariant param0) {
        return this.getSprite(Registry.PAINTING_VARIANT.getKey(param0));
    }

    public TextureAtlasSprite getBackSprite() {
        return this.getSprite(BACK_SPRITE_LOCATION);
    }
}
