package net.minecraft.gametest.framework;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class GameTestBatchRunner {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final BlockPos firstTestNorthWestCorner;
    final ServerLevel level;
    private final GameTestTicker testTicker;
    private final int testsPerRow;
    private final List<GameTestInfo> allTestInfos;
    private final List<Pair<GameTestBatch, Collection<GameTestInfo>>> batches;
    private int count;
    private AABB rowBounds;
    private final BlockPos.MutableBlockPos nextTestNorthWestCorner;

    public GameTestBatchRunner(Collection<GameTestBatch> param0, BlockPos param1, Rotation param2, ServerLevel param3, GameTestTicker param4, int param5) {
        this.nextTestNorthWestCorner = param1.mutable();
        this.rowBounds = new AABB(this.nextTestNorthWestCorner);
        this.firstTestNorthWestCorner = param1;
        this.level = param3;
        this.testTicker = param4;
        this.testsPerRow = param5;
        this.batches = param0.stream()
            .map(
                param2x -> {
                    Collection<GameTestInfo> var0 = param2x.getTestFunctions()
                        .stream()
                        .map(param2xx -> new GameTestInfo(param2xx, param2, param3))
                        .collect(ImmutableList.toImmutableList());
                    return Pair.of(param2x, var0);
                }
            )
            .collect(ImmutableList.toImmutableList());
        this.allTestInfos = this.batches.stream().flatMap(param0x -> param0x.getSecond().stream()).collect(ImmutableList.toImmutableList());
    }

    public List<GameTestInfo> getTestInfos() {
        return this.allTestInfos;
    }

    public void start() {
        this.runBatch(0);
    }

    void runBatch(final int param0) {
        if (param0 < this.batches.size()) {
            Pair<GameTestBatch, Collection<GameTestInfo>> var0 = this.batches.get(param0);
            final GameTestBatch var1 = var0.getFirst();
            Collection<GameTestInfo> var2 = var0.getSecond();
            Map<GameTestInfo, BlockPos> var3 = this.createStructuresForBatch(var2);
            String var4 = var1.getName();
            LOGGER.info("Running test batch '{}' ({} tests)...", var4, var2.size());
            var1.runBeforeBatchFunction(this.level);
            final MultipleTestTracker var5 = new MultipleTestTracker();
            var2.forEach(var5::addTestToTrack);
            var5.addListener(new GameTestListener() {
                private void testCompleted() {
                    if (var5.isDone()) {
                        var1.runAfterBatchFunction(GameTestBatchRunner.this.level);
                        LongSet var0 = new LongArraySet(GameTestBatchRunner.this.level.getForcedChunks());
                        var0.forEach(param0xx -> GameTestBatchRunner.this.level.setChunkForced(ChunkPos.getX(param0xx), ChunkPos.getZ(param0xx), false));
                        GameTestBatchRunner.this.runBatch(param0 + 1);
                    }

                }

                @Override
                public void testStructureLoaded(GameTestInfo param0x) {
                }

                @Override
                public void testPassed(GameTestInfo param0x) {
                    this.testCompleted();
                }

                @Override
                public void testFailed(GameTestInfo param0x) {
                    this.testCompleted();
                }
            });
            var2.forEach(param1 -> {
                BlockPos var0x = var3.get(param1);
                GameTestRunner.runTest(param1, var0x, this.testTicker);
            });
        }
    }

    private Map<GameTestInfo, BlockPos> createStructuresForBatch(Collection<GameTestInfo> param0) {
        Map<GameTestInfo, BlockPos> var0 = Maps.newHashMap();

        for(GameTestInfo var1 : param0) {
            BlockPos var2 = new BlockPos(this.nextTestNorthWestCorner);
            StructureBlockEntity var3 = StructureUtils.prepareTestStructure(var1, var2, var1.getRotation(), this.level);
            AABB var4 = StructureUtils.getStructureBounds(var3);
            var1.setStructureBlockPos(var3.getBlockPos());
            var0.put(var1, new BlockPos(this.nextTestNorthWestCorner));
            this.rowBounds = this.rowBounds.minmax(var4);
            this.nextTestNorthWestCorner.move((int)var4.getXsize() + 5, 0, 0);
            if (this.count++ % this.testsPerRow == this.testsPerRow - 1) {
                this.nextTestNorthWestCorner.move(0, 0, (int)this.rowBounds.getZsize() + 6);
                this.nextTestNorthWestCorner.setX(this.firstTestNorthWestCorner.getX());
                this.rowBounds = new AABB(this.nextTestNorthWestCorner);
            }
        }

        return var0;
    }
}
