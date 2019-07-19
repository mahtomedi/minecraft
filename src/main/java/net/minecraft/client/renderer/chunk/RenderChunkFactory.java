package net.minecraft.client.renderer.chunk;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface RenderChunkFactory {
    RenderChunk create(Level var1, LevelRenderer var2);
}
