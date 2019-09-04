package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import java.util.List;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChunkRenderList {
    private double xOff;
    private double yOff;
    private double zOff;
    protected final List<RenderChunk> chunks = Lists.newArrayListWithCapacity(17424);
    protected boolean ready;

    public void setCameraLocation(double param0, double param1, double param2) {
        this.ready = true;
        this.chunks.clear();
        this.xOff = param0;
        this.yOff = param1;
        this.zOff = param2;
    }

    public void translateToRelativeChunkPosition(RenderChunk param0) {
        BlockPos var0 = param0.getOrigin();
        RenderSystem.translatef((float)((double)var0.getX() - this.xOff), (float)((double)var0.getY() - this.yOff), (float)((double)var0.getZ() - this.zOff));
    }

    public void add(RenderChunk param0, BlockLayer param1) {
        this.chunks.add(param0);
    }

    public void render(BlockLayer param0) {
        if (this.ready) {
            for(RenderChunk var0 : this.chunks) {
                VertexBuffer var1 = var0.getBuffer(param0.ordinal());
                RenderSystem.pushMatrix();
                this.translateToRelativeChunkPosition(var0);
                var1.bind();
                this.applyVertexDeclaration();
                var1.draw(7);
                RenderSystem.popMatrix();
            }

            VertexBuffer.unbind();
            RenderSystem.clearCurrentColor();
            this.chunks.clear();
        }
    }

    private void applyVertexDeclaration() {
        RenderSystem.vertexPointer(3, 5126, 28, 0);
        RenderSystem.colorPointer(4, 5121, 28, 12);
        RenderSystem.texCoordPointer(2, 5126, 28, 16);
        RenderSystem.glClientActiveTexture(33985);
        RenderSystem.texCoordPointer(2, 5122, 28, 24);
        RenderSystem.glClientActiveTexture(33984);
    }
}
