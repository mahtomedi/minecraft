package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.world.level.BlockLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChunkBufferBuilderPack {
    private final BufferBuilder[] builders = new BufferBuilder[BlockLayer.values().length];

    public ChunkBufferBuilderPack() {
        this.builders[BlockLayer.SOLID.ordinal()] = new BufferBuilder(2097152);
        this.builders[BlockLayer.CUTOUT.ordinal()] = new BufferBuilder(131072);
        this.builders[BlockLayer.CUTOUT_MIPPED.ordinal()] = new BufferBuilder(131072);
        this.builders[BlockLayer.TRANSLUCENT.ordinal()] = new BufferBuilder(262144);
    }

    public BufferBuilder builder(BlockLayer param0) {
        return this.builders[param0.ordinal()];
    }

    public BufferBuilder builder(int param0) {
        return this.builders[param0];
    }
}
