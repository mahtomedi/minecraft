package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class NoiseGeneratorSettings {
    private final ChunkGeneratorSettings structureSettings;
    protected BlockState defaultBlock = Blocks.STONE.defaultBlockState();
    protected BlockState defaultFluid = Blocks.WATER.defaultBlockState();

    public NoiseGeneratorSettings(ChunkGeneratorSettings param0) {
        this.structureSettings = param0;
    }

    public BlockState getDefaultBlock() {
        return this.defaultBlock;
    }

    public BlockState getDefaultFluid() {
        return this.defaultFluid;
    }

    public void setDefaultBlock(BlockState param0) {
        this.defaultBlock = param0;
    }

    public void setDefaultFluid(BlockState param0) {
        this.defaultFluid = param0;
    }

    public int getBedrockRoofPosition() {
        return 0;
    }

    public int getBedrockFloorPosition() {
        return 256;
    }

    public ChunkGeneratorSettings structureSettings() {
        return this.structureSettings;
    }
}
