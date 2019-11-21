package net.minecraft.client.renderer.texture;

import java.util.Collection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StitcherException extends RuntimeException {
    private final Collection<TextureAtlasSprite.Info> allSprites;

    public StitcherException(TextureAtlasSprite.Info param0, Collection<TextureAtlasSprite.Info> param1) {
        super(String.format("Unable to fit: %s - size: %dx%d - Maybe try a lower resolution resourcepack?", param0.name(), param0.width(), param0.height()));
        this.allSprites = param1;
    }

    public Collection<TextureAtlasSprite.Info> getAllSprites() {
        return this.allSprites;
    }
}
