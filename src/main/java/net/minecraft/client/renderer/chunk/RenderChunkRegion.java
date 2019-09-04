package net.minecraft.client.renderer.chunk;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndBiomeGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderChunkRegion implements BlockAndBiomeGetter {
    protected final int centerX;
    protected final int centerZ;
    protected final BlockPos start;
    protected final int xLength;
    protected final int yLength;
    protected final int zLength;
    protected final LevelChunk[][] chunks;
    protected final BlockState[] blockStates;
    protected final FluidState[] fluidStates;
    protected final Level level;

    @Nullable
    public static RenderChunkRegion createIfNotEmpty(Level param0, BlockPos param1, BlockPos param2, int param3) {
        int var0 = param1.getX() - param3 >> 4;
        int var1 = param1.getZ() - param3 >> 4;
        int var2 = param2.getX() + param3 >> 4;
        int var3 = param2.getZ() + param3 >> 4;
        LevelChunk[][] var4 = new LevelChunk[var2 - var0 + 1][var3 - var1 + 1];

        for(int var5 = var0; var5 <= var2; ++var5) {
            for(int var6 = var1; var6 <= var3; ++var6) {
                var4[var5 - var0][var6 - var1] = param0.getChunk(var5, var6);
            }
        }

        boolean var7 = true;

        for(int var8 = param1.getX() >> 4; var8 <= param2.getX() >> 4; ++var8) {
            for(int var9 = param1.getZ() >> 4; var9 <= param2.getZ() >> 4; ++var9) {
                LevelChunk var10 = var4[var8 - var0][var9 - var1];
                if (!var10.isYSpaceEmpty(param1.getY(), param2.getY())) {
                    var7 = false;
                }
            }
        }

        if (var7) {
            return null;
        } else {
            int var11 = 1;
            BlockPos var12 = param1.offset(-1, -1, -1);
            BlockPos var13 = param2.offset(1, 1, 1);
            return new RenderChunkRegion(param0, var0, var1, var4, var12, var13);
        }
    }

    public RenderChunkRegion(Level param0, int param1, int param2, LevelChunk[][] param3, BlockPos param4, BlockPos param5) {
        this.level = param0;
        this.centerX = param1;
        this.centerZ = param2;
        this.chunks = param3;
        this.start = param4;
        this.xLength = param5.getX() - param4.getX() + 1;
        this.yLength = param5.getY() - param4.getY() + 1;
        this.zLength = param5.getZ() - param4.getZ() + 1;
        this.blockStates = new BlockState[this.xLength * this.yLength * this.zLength];
        this.fluidStates = new FluidState[this.xLength * this.yLength * this.zLength];

        for(BlockPos var0 : BlockPos.betweenClosed(param4, param5)) {
            int var1 = (var0.getX() >> 4) - param1;
            int var2 = (var0.getZ() >> 4) - param2;
            LevelChunk var3 = param3[var1][var2];
            int var4 = this.index(var0);
            this.blockStates[var4] = var3.getBlockState(var0);
            this.fluidStates[var4] = var3.getFluidState(var0);
        }

    }

    protected final int index(BlockPos param0) {
        return this.index(param0.getX(), param0.getY(), param0.getZ());
    }

    protected int index(int param0, int param1, int param2) {
        int var0 = param0 - this.start.getX();
        int var1 = param1 - this.start.getY();
        int var2 = param2 - this.start.getZ();
        return var2 * this.xLength * this.yLength + var1 * this.xLength + var0;
    }

    @Override
    public BlockState getBlockState(BlockPos param0) {
        return this.blockStates[this.index(param0)];
    }

    @Override
    public FluidState getFluidState(BlockPos param0) {
        return this.fluidStates[this.index(param0)];
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.level.getLightEngine();
    }

    @Override
    public BiomeManager getBiomeManager() {
        return this.level.getBiomeManager();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos param0) {
        return this.getBlockEntity(param0, LevelChunk.EntityCreationType.IMMEDIATE);
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos param0, LevelChunk.EntityCreationType param1) {
        int var0 = (param0.getX() >> 4) - this.centerX;
        int var1 = (param0.getZ() >> 4) - this.centerZ;
        return this.chunks[var0][var1].getBlockEntity(param0, param1);
    }
}
