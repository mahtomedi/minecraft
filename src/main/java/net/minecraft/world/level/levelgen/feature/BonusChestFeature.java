package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BonusChestFeature extends Feature<NoneFeatureConfiguration> {
    public BonusChestFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4
    ) {
        for(BlockState var0 = param0.getBlockState(param3);
            (var0.isAir() || var0.is(BlockTags.LEAVES)) && param3.getY() > 1;
            var0 = param0.getBlockState(param3)
        ) {
            param3 = param3.below();
        }

        if (param3.getY() < 1) {
            return false;
        } else {
            param3 = param3.above();

            for(int var1 = 0; var1 < 4; ++var1) {
                BlockPos var2 = param3.offset(
                    param2.nextInt(4) - param2.nextInt(4), param2.nextInt(3) - param2.nextInt(3), param2.nextInt(4) - param2.nextInt(4)
                );
                if (param0.isEmptyBlock(var2)) {
                    param0.setBlock(var2, Blocks.CHEST.defaultBlockState(), 2);
                    RandomizableContainerBlockEntity.setLootTable(param0, param2, var2, BuiltInLootTables.SPAWN_BONUS_CHEST);
                    BlockState var3 = Blocks.TORCH.defaultBlockState();

                    for(Direction var4 : Direction.Plane.HORIZONTAL) {
                        BlockPos var5 = var2.relative(var4);
                        if (var3.canSurvive(param0, var5)) {
                            param0.setBlock(var5, var3, 2);
                        }
                    }

                    return true;
                }
            }

            return false;
        }
    }
}
