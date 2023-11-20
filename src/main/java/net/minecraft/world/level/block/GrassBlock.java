package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class GrassBlock extends SpreadingSnowyDirtBlock implements BonemealableBlock {
    public static final MapCodec<GrassBlock> CODEC = simpleCodec(GrassBlock::new);

    @Override
    public MapCodec<GrassBlock> codec() {
        return CODEC;
    }

    public GrassBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader param0, BlockPos param1, BlockState param2) {
        return param0.getBlockState(param1.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level param0, RandomSource param1, BlockPos param2, BlockState param3) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel param0, RandomSource param1, BlockPos param2, BlockState param3) {
        BlockPos var0 = param2.above();
        BlockState var1 = Blocks.SHORT_GRASS.defaultBlockState();
        Optional<Holder.Reference<PlacedFeature>> var2 = param0.registryAccess()
            .registryOrThrow(Registries.PLACED_FEATURE)
            .getHolder(VegetationPlacements.GRASS_BONEMEAL);

        label49:
        for(int var3 = 0; var3 < 128; ++var3) {
            BlockPos var4 = var0;

            for(int var5 = 0; var5 < var3 / 16; ++var5) {
                var4 = var4.offset(param1.nextInt(3) - 1, (param1.nextInt(3) - 1) * param1.nextInt(3) / 2, param1.nextInt(3) - 1);
                if (!param0.getBlockState(var4.below()).is(this) || param0.getBlockState(var4).isCollisionShapeFullBlock(param0, var4)) {
                    continue label49;
                }
            }

            BlockState var6 = param0.getBlockState(var4);
            if (var6.is(var1.getBlock()) && param1.nextInt(10) == 0) {
                ((BonemealableBlock)var1.getBlock()).performBonemeal(param0, param1, var4, var6);
            }

            if (var6.isAir()) {
                Holder<PlacedFeature> var8;
                if (param1.nextInt(8) == 0) {
                    List<ConfiguredFeature<?, ?>> var7 = param0.getBiome(var4).value().getGenerationSettings().getFlowerFeatures();
                    if (var7.isEmpty()) {
                        continue;
                    }

                    var8 = ((RandomPatchConfiguration)var7.get(0).config()).feature();
                } else {
                    if (!var2.isPresent()) {
                        continue;
                    }

                    var8 = var2.get();
                }

                var8.value().place(param0, param0.getChunkSource().getGenerator(), param1, var4);
            }
        }

    }
}
