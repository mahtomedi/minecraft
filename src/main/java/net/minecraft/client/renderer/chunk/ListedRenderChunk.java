package net.minecraft.client.renderer.chunk;

import com.mojang.blaze3d.platform.MemoryTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ListedRenderChunk extends RenderChunk {
    private final int listId = MemoryTracker.genLists(BlockLayer.values().length);

    public ListedRenderChunk(Level param0, LevelRenderer param1) {
        super(param0, param1);
    }

    public int getGlListId(BlockLayer param0, CompiledChunk param1) {
        return !param1.isEmpty(param0) ? this.listId + param0.ordinal() : -1;
    }

    @Override
    public void releaseBuffers() {
        super.releaseBuffers();
        MemoryTracker.releaseLists(this.listId, BlockLayer.values().length);
    }
}
