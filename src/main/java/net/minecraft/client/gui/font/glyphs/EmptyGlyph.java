package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class EmptyGlyph extends BakedGlyph {
    public static final EmptyGlyph INSTANCE = new EmptyGlyph();

    public EmptyGlyph() {
        super(GlyphRenderTypes.createForColorTexture(new ResourceLocation("")), 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
    }

    @Override
    public void render(
        boolean param0, float param1, float param2, Matrix4f param3, VertexConsumer param4, float param5, float param6, float param7, float param8, int param9
    ) {
    }
}
