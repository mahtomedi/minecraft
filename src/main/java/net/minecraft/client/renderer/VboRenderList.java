package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.world.level.BlockLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VboRenderList extends ChunkRenderList {
    @Override
    public void render(BlockLayer param0) {
        if (this.ready) {
            for(RenderChunk var0 : this.chunks) {
                VertexBuffer var1 = var0.getBuffer(param0.ordinal());
                GlStateManager.pushMatrix();
                this.translateToRelativeChunkPosition(var0);
                var1.bind();
                this.applyVertexDeclaration();
                var1.draw(7);
                GlStateManager.popMatrix();
            }

            VertexBuffer.unbind();
            GlStateManager.clearCurrentColor();
            this.chunks.clear();
        }
    }

    private void applyVertexDeclaration() {
        GlStateManager.vertexPointer(3, 5126, 28, 0);
        GlStateManager.colorPointer(4, 5121, 28, 12);
        GlStateManager.texCoordPointer(2, 5126, 28, 16);
        GLX.glClientActiveTexture(GLX.GL_TEXTURE1);
        GlStateManager.texCoordPointer(2, 5122, 28, 24);
        GLX.glClientActiveTexture(GLX.GL_TEXTURE0);
    }
}
