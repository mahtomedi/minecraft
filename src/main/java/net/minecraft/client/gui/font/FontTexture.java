package net.minecraft.client.gui.font;

import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FontTexture extends AbstractTexture implements Dumpable {
    private static final int SIZE = 256;
    private final GlyphRenderTypes renderTypes;
    private final boolean colored;
    private final FontTexture.Node root;

    public FontTexture(GlyphRenderTypes param0, boolean param1) {
        this.colored = param1;
        this.root = new FontTexture.Node(0, 0, 256, 256);
        TextureUtil.prepareImage(param1 ? NativeImage.InternalGlFormat.RGBA : NativeImage.InternalGlFormat.RED, this.getId(), 256, 256);
        this.renderTypes = param0;
    }

    @Override
    public void load(ResourceManager param0) {
    }

    @Override
    public void close() {
        this.releaseId();
    }

    @Nullable
    public BakedGlyph add(SheetGlyphInfo param0) {
        if (param0.isColored() != this.colored) {
            return null;
        } else {
            FontTexture.Node var0 = this.root.insert(param0);
            if (var0 != null) {
                this.bind();
                param0.upload(var0.x, var0.y);
                float var1 = 256.0F;
                float var2 = 256.0F;
                float var3 = 0.01F;
                return new BakedGlyph(
                    this.renderTypes,
                    ((float)var0.x + 0.01F) / 256.0F,
                    ((float)var0.x - 0.01F + (float)param0.getPixelWidth()) / 256.0F,
                    ((float)var0.y + 0.01F) / 256.0F,
                    ((float)var0.y - 0.01F + (float)param0.getPixelHeight()) / 256.0F,
                    param0.getLeft(),
                    param0.getRight(),
                    param0.getUp(),
                    param0.getDown()
                );
            } else {
                return null;
            }
        }
    }

    @Override
    public void dumpContents(ResourceLocation param0, Path param1) {
        String var0 = param0.toDebugFileName();
        TextureUtil.writeAsPNG(param1, var0, this.getId(), 0, 256, 256, param0x -> (param0x & 0xFF000000) == 0 ? -16777216 : param0x);
    }

    @OnlyIn(Dist.CLIENT)
    static class Node {
        final int x;
        final int y;
        private final int width;
        private final int height;
        @Nullable
        private FontTexture.Node left;
        @Nullable
        private FontTexture.Node right;
        private boolean occupied;

        Node(int param0, int param1, int param2, int param3) {
            this.x = param0;
            this.y = param1;
            this.width = param2;
            this.height = param3;
        }

        @Nullable
        FontTexture.Node insert(SheetGlyphInfo param0) {
            if (this.left != null && this.right != null) {
                FontTexture.Node var0 = this.left.insert(param0);
                if (var0 == null) {
                    var0 = this.right.insert(param0);
                }

                return var0;
            } else if (this.occupied) {
                return null;
            } else {
                int var1 = param0.getPixelWidth();
                int var2 = param0.getPixelHeight();
                if (var1 > this.width || var2 > this.height) {
                    return null;
                } else if (var1 == this.width && var2 == this.height) {
                    this.occupied = true;
                    return this;
                } else {
                    int var3 = this.width - var1;
                    int var4 = this.height - var2;
                    if (var3 > var4) {
                        this.left = new FontTexture.Node(this.x, this.y, var1, this.height);
                        this.right = new FontTexture.Node(this.x + var1 + 1, this.y, this.width - var1 - 1, this.height);
                    } else {
                        this.left = new FontTexture.Node(this.x, this.y, this.width, var2);
                        this.right = new FontTexture.Node(this.x, this.y + var2 + 1, this.width, this.height - var2 - 1);
                    }

                    return this.left.insert(param0);
                }
            }
        }
    }
}
