package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class OverworldGeneratorSettings extends ChunkGeneratorSettings {
    private final int biomeSize;
    private final int riverSize;
    private final int fixedBiome = -1;
    private final int seaLevel;
    public static final List<BlockState> SAFE_BLOCKS = Registry.BLOCK
        .stream()
        .filter(param0 -> !param0.isUnstable() && !param0.isEntityBlock())
        .map(Block::defaultBlockState)
        .filter(param0 -> !PoiType.isPoi(param0))
        .collect(ImmutableList.toImmutableList());
    public static final List<BlockState> GROUND_BLOCKS = Registry.BLOCK
        .stream()
        .filter(param0 -> !param0.isUnstable() && !param0.isEntityBlock() && !param0.hasDynamicShape())
        .map(Block::defaultBlockState)
        .filter(param0 -> !PoiType.isPoi(param0) && param0.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO))
        .collect(ImmutableList.toImmutableList());

    public OverworldGeneratorSettings() {
        this.seaLevel = 63;
        this.biomeSize = 4;
        this.riverSize = 4;
    }

    public OverworldGeneratorSettings(Random param0) {
        this.seaLevel = param0.nextInt(128);
        this.biomeSize = param0.nextInt(8) + 1;
        this.riverSize = param0.nextInt(8) + 1;
        this.defaultBlock = this.randomGroundBlock(param0);
        this.defaultFluid = this.randomLiquidBlock(param0);
    }

    public int getBiomeSize() {
        return this.biomeSize;
    }

    public int getRiverSize() {
        return this.riverSize;
    }

    public int getFixedBiome() {
        return -1;
    }

    @Override
    public int getBedrockFloorPosition() {
        return 0;
    }
}
