package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.vertex.BufferBuilder;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EmptyGlyph extends BakedGlyph {
    public EmptyGlyph() {
        super(new ResourceLocation(""), 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
    }

    @Override
    public void render(
        TextureManager param0, boolean param1, float param2, float param3, BufferBuilder param4, float param5, float param6, float param7, float param8
    ) {
    }

    @Nullable
    @Override
    public ResourceLocation getTexture() {
        return null;
    }
}
