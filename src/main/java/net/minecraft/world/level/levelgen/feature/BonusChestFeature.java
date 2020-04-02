package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BonusChestFeature extends Feature<NoneFeatureConfiguration> {
    public BonusChestFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0,
        StructureFeatureManager param1,
        ChunkGenerator<? extends ChunkGeneratorSettings> param2,
        Random param3,
        BlockPos param4,
        NoneFeatureConfiguration param5
    ) {
        ChunkPos var0 = new ChunkPos(param4);
        List<Integer> var1 = IntStream.rangeClosed(var0.getMinBlockX(), var0.getMaxBlockX()).boxed().collect(Collectors.toList());
        Collections.shuffle(var1, param3);
        List<Integer> var2 = IntStream.rangeClosed(var0.getMinBlockZ(), var0.getMaxBlockZ()).boxed().collect(Collectors.toList());
        Collections.shuffle(var2, param3);
        BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos();

        for(Integer var4 : var1) {
            for(Integer var5 : var2) {
                var3.set(var4, 0, var5);
                BlockPos var6 = param0.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, var3);
                if (param0.isEmptyBlock(var6) || param0.getBlockState(var6).getCollisionShape(param0, var6).isEmpty()) {
                    param0.setBlock(var6, Blocks.CHEST.defaultBlockState(), 2);
                    RandomizableContainerBlockEntity.setLootTable(param0, param3, var6, BuiltInLootTables.SPAWN_BONUS_CHEST);
                    BlockState var7 = Blocks.TORCH.defaultBlockState();

                    for(Direction var8 : Direction.Plane.HORIZONTAL) {
                        BlockPos var9 = var6.relative(var8);
                        if (var7.canSurvive(param0, var9)) {
                            param0.setBlock(var9, var7, 2);
                        }
                    }

                    return true;
                }
            }
        }

        return false;
    }
}
