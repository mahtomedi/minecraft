package net.minecraft.client.renderer.texture;

import java.util.Collection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StitcherException extends RuntimeException {
    private final Collection<TextureAtlasSprite> allSprites;

    public StitcherException(TextureAtlasSprite param0, Collection<TextureAtlasSprite> param1) {
        super(
            String.format(
                "Unable to fit: %s - size: %dx%d - Maybe try a lower resolution resourcepack?", param0.getName(), param0.getWidth(), param0.getHeight()
            )
        );
        this.allSprites = param1;
    }

    public Collection<TextureAtlasSprite> getAllSprites() {
        return this.allSprites;
    }
}
