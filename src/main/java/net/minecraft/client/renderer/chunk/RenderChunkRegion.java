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
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderChunkRegion implements BlockAndTintGetter {
    protected final int centerX;
    protected final int centerZ;
    protected final LevelChunk[][] chunks;
    protected final Level level;

    @Nullable
    public static RenderChunkRegion createIfNotEmpty(Level param0, BlockPos param1, BlockPos param2, int param3) {
        int var0 = SectionPos.blockToSectionCoord(param1.getX() - param3);
        int var1 = SectionPos.blockToSectionCoord(param1.getZ() - param3);
        int var2 = SectionPos.blockToSectionCoord(param2.getX() + param3);
        int var3 = SectionPos.blockToSectionCoord(param2.getZ() + param3);
        LevelChunk[][] var4 = new LevelChunk[var2 - var0 + 1][var3 - var1 + 1];

        for(int var5 = var0; var5 <= var2; ++var5) {
            for(int var6 = var1; var6 <= var3; ++var6) {
                var4[var5 - var0][var6 - var1] = param0.getChunk(var5, var6);
            }
        }

        return isAllEmpty(param1, param2, var0, var1, var4) ? null : new RenderChunkRegion(param0, var0, var1, var4);
    }

    public static boolean isAllEmpty(BlockPos param0, BlockPos param1, int param2, int param3, LevelChunk[][] param4) {
        for(int var0 = SectionPos.blockToSectionCoord(param0.getX()); var0 <= SectionPos.blockToSectionCoord(param1.getX()); ++var0) {
            for(int var1 = SectionPos.blockToSectionCoord(param0.getZ()); var1 <= SectionPos.blockToSectionCoord(param1.getZ()); ++var1) {
                LevelChunk var2 = param4[var0 - param2][var1 - param3];
                if (!var2.isYSpaceEmpty(param0.getY(), param1.getY())) {
                    return false;
                }
            }
        }

        return true;
    }

    public RenderChunkRegion(Level param0, int param1, int param2, LevelChunk[][] param3) {
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
        return this.getBlockEntity(param0, LevelChunk.EntityCreationType.IMMEDIATE);
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos param0, LevelChunk.EntityCreationType param1) {
        int var0 = SectionPos.blockToSectionCoord(param0.getX()) - this.centerX;
        int var1 = SectionPos.blockToSectionCoord(param0.getZ()) - this.centerZ;
        return this.chunks[var0][var1].getBlockEntity(param0, param1);
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
