package net.minecraft.client.renderer.chunk;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderChunkRegion implements BlockAndTintGetter {
    private final int centerX;
    private final int centerZ;
    protected final RenderChunk[][] chunks;
    protected final Level level;

    RenderChunkRegion(Level param0, int param1, int param2, RenderChunk[][] param3) {
        this.level = param0;
        this.centerX = param1;
        this.centerZ = param2;
        this.chunks = param3;
    }

    @Override
    public BlockState getBlockState(BlockPos param0) {
        int var0 = SectionPos.blockToSectionCoord(param0.getX()) - this.centerX;
        int var1 = SectionPos.blockToSectionCoord(param0.getZ()) - this.centerZ;
        return this.chunks[var0][var1].getBlockState(param0);
    }

    @Override
    public FluidState getFluidState(BlockPos param0) {
        int var0 = SectionPos.blockToSectionCoord(param0.getX()) - this.centerX;
        int var1 = SectionPos.blockToSectionCoord(param0.getZ()) - this.centerZ;
        return this.chunks[var0][var1].getBlockState(param0).getFluidState();
    }

    @Override
    public float getShade(Direction param0, boolean param1) {
        return this.level.getShade(param0, param1);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.level.getLightEngine();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos param0) {
        int var0 = SectionPos.blockToSectionCoord(param0.getX()) - this.centerX;
        int var1 = SectionPos.blockToSectionCoord(param0.getZ()) - this.centerZ;
        return this.chunks[var0][var1].getBlockEntity(param0);
    }

    @Override
    public int getBlockTint(BlockPos param0, ColorResolver param1) {
        return this.level.getBlockTint(param0, param1);
    }

    @Override
    public int getMinBuildHeight() {
        return this.level.getMinBuildHeight();
    }

    @Override
    public int getHeight() {
        return this.level.getHeight();
    }
}
