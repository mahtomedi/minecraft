package net.minecraft.client.gui.font;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record GlyphRenderTypes(RenderType normal, RenderType seeThrough, RenderType polygonOffset) {
    public static GlyphRenderTypes createForIntensityTexture(ResourceLocation param0) {
        return new GlyphRenderTypes(RenderType.textIntensity(param0), RenderType.textIntensitySeeThrough(param0), RenderType.textIntensityPolygonOffset(param0));
    }

    public static GlyphRenderTypes createForColorTexture(ResourceLocation param0) {
        return new GlyphRenderTypes(RenderType.text(param0), RenderType.textSeeThrough(param0), RenderType.textPolygonOffset(param0));
    }

    public RenderType select(Font.DisplayMode param0) {
        return switch(param0) {
            case NORMAL -> this.normal;
            case SEE_THROUGH -> this.seeThrough;
            case POLYGON_OFFSET -> this.polygonOffset;
        };
    }
}
