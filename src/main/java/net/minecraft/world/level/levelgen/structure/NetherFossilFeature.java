package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class NetherFossilFeature extends StructureFeature<RangeDecoratorConfiguration> {
    public NetherFossilFeature(Codec<RangeDecoratorConfiguration> param0) {
        super(param0);
    }

    @Override
    public StructureFeature.StructureStartFactory<RangeDecoratorConfiguration> getStartFactory() {
        return NetherFossilFeature.FeatureStart::new;
    }

    public static class FeatureStart extends NoiseAffectingStructureStart<RangeDecoratorConfiguration> {
        public FeatureStart(StructureFeature<RangeDecoratorConfiguration> param0, ChunkPos param1, int param2, long param3) {
            super(param0, param1, param2, param3);
        }

        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            ChunkPos param3,
            RangeDecoratorConfiguration param4,
            LevelHeightAccessor param5,
            Predicate<Biome> param6
        ) {
            int var0 = param3.getMinBlockX() + this.random.nextInt(16);
            int var1 = param3.getMinBlockZ() + this.random.nextInt(16);
            int var2 = param1.getSeaLevel();
            WorldGenerationContext var3 = new WorldGenerationContext(param1, param5);
            int var4 = param4.height.sample(this.random, var3);
            NoiseColumn var5 = param1.getBaseColumn(var0, var1, param5);
            BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos(var0, var4, var1);

            while(var4 > var2) {
                BlockState var7 = var5.getBlock(var4);
                BlockState var8 = var5.getBlock(--var4);
                if (var7.isAir() && (var8.is(Blocks.SOUL_SAND) || var8.isFaceSturdy(EmptyBlockGetter.INSTANCE, var6.setY(var4), Direction.UP))) {
                    break;
                }
            }

            if (var4 > var2) {
                if (param6.test(param1.getNoiseBiome(QuartPos.fromBlock(var0), QuartPos.fromBlock(var4), QuartPos.fromBlock(var1)))) {
                    NetherFossilPieces.addPieces(param2, this, this.random, new BlockPos(var0, var4, var1));
                }
            }
        }
    }
}
