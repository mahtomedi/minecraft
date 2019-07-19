package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.chunk.ListedRenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.world.level.BlockLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OffsettedRenderList extends ChunkRenderList {
    @Override
    public void render(BlockLayer param0) {
        if (this.ready) {
            for(RenderChunk var0 : this.chunks) {
                ListedRenderChunk var1 = (ListedRenderChunk)var0;
                GlStateManager.pushMatrix();
                this.translateToRelativeChunkPosition(var0);
                GlStateManager.callList(var1.getGlListId(param0, var1.getCompiledChunk()));
                GlStateManager.popMatrix();
            }

            GlStateManager.clearCurrentColor();
            this.chunks.clear();
        }
    }
}
