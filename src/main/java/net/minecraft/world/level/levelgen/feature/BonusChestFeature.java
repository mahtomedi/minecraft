package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BonusChestFeature extends Feature<NoneFeatureConfiguration> {
    public BonusChestFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> param0) {
        RandomSource var0 = param0.random();
        WorldGenLevel var1 = param0.level();
        ChunkPos var2 = new ChunkPos(param0.origin());
        IntArrayList var3 = Util.toShuffledList(IntStream.rangeClosed(var2.getMinBlockX(), var2.getMaxBlockX()), var0);
        IntArrayList var4 = Util.toShuffledList(IntStream.rangeClosed(var2.getMinBlockZ(), var2.getMaxBlockZ()), var0);
        BlockPos.MutableBlockPos var5 = new BlockPos.MutableBlockPos();

        for(Integer var6 : var3) {
            for(Integer var7 : var4) {
                var5.set(var6, 0, var7);
                BlockPos var8 = var1.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, var5);
                if (var1.isEmptyBlock(var8) || var1.getBlockState(var8).getCollisionShape(var1, var8).isEmpty()) {
                    var1.setBlock(var8, Blocks.CHEST.defaultBlockState(), 2);
                    RandomizableContainer.setBlockEntityLootTable(var1, var0, var8, BuiltInLootTables.SPAWN_BONUS_CHEST);
                    BlockState var9 = Blocks.TORCH.defaultBlockState();

                    for(Direction var10 : Direction.Plane.HORIZONTAL) {
                        BlockPos var11 = var8.relative(var10);
                        if (var9.canSurvive(var1, var11)) {
                            var1.setBlock(var11, var9, 2);
                        }
                    }

                    return true;
                }
            }
        }

        return false;
    }
}
