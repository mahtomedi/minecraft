package net.minecraft.world.level.dimension.special;

import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldLevelSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G25 extends NormalDimension {
    private static final Vector3f RED = new Vector3f(1.0F, 0.0F, 0.0F);
    private static final Vector3f BLUE = new Vector3f(0.0F, 0.0F, 1.0F);

    public G25(Level param0, DimensionType param1) {
        super(param0, param1);
    }

    @Override
    public ChunkGenerator<? extends ChunkGeneratorSettings> createRandomLevelGenerator() {
        return new G25.Generator(this.level, SpecialDimensionBase.normalBiomes(this.level.getSeed()), ChunkGeneratorType.SURFACE.createSettings());
    }

    @OnlyIn(Dist.CLIENT)
    private static Vector3f getHalfColor(BlockPos param0) {
        if (param0.getX() > 0) {
            return RED;
        } else {
            return param0.getX() < 0 ? BLUE : NO_CHANGE;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vector3f getExtraTint(BlockState param0, BlockPos param1) {
        return getHalfColor(param1);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <T extends LivingEntity> Vector3f getEntityExtraTint(T param0) {
        return getHalfColor(param0.blockPosition());
    }

    public static class Generator extends OverworldLevelSource {
        public Generator(LevelAccessor param0, BiomeSource param1, OverworldGeneratorSettings param2) {
            super(param0, param1, param2);
        }

        @Override
        public void applyBiomeDecoration(WorldGenRegion param0) {
            super.applyBiomeDecoration(param0);
            int var0 = param0.getCenterX();
            int var1 = param0.getCenterZ();
            if (var0 == 0) {
                BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();

                for(int var3 = 0; var3 < 16; ++var3) {
                    for(int var4 = 0; var4 < 256; ++var4) {
                        param0.setBlock(var2.set(0, var4, 16 * var1 + var3), Blocks.BEDROCK.defaultBlockState(), 4);
                    }
                }

                if (var1 == 0) {
                    BlockPos var5 = param0.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, var2.set(0, 0, 0));
                    BlockState var6 = Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.EAST);
                    param0.setBlock(var5, var6.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 4);
                    param0.setBlock(var5.above(), var6.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 4);
                }
            }

        }

        @Override
        public ChunkGeneratorType<?, ?> getType() {
            return ChunkGeneratorType.T20;
        }
    }
}
